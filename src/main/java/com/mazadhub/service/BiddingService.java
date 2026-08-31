package com.mazadhub.service;

import com.mazadhub.bidding.AuctionSnapshot;
import com.mazadhub.bidding.BidResolution;
import com.mazadhub.bidding.BidValidator;
import com.mazadhub.bidding.ProxyBidEngine;
import com.mazadhub.bidding.StandingBid;
import com.mazadhub.domain.AutoBid;
import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import com.mazadhub.exception.AlreadyHighestBidderException;
import com.mazadhub.exception.SellerCannotBidException;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BuyNowNotAvailableException;
import com.mazadhub.exception.ItemNotFoundException;
import com.mazadhub.exception.UserNotFoundException;
import com.mazadhub.notification.NotificationPort;
import com.mazadhub.pricing.PriceIncrementRules;
import com.mazadhub.repository.AutoBidRepository;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.repository.ItemRepository;
import com.mazadhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

// The heart of the system: takes a bid, validates it, runs the proxy engine and saves the result
// Every public action runs in one transaction on a row-locked item, so racing bids stay consistent
@ApplicationScoped
public class BiddingService
{
    private ItemRepository items;
    private BidRepository bids;
    private AutoBidRepository autoBids;
    private UserRepository users;
    private NotificationPort notifier;

    // the price-step table and the two engines built on top of it
    private final PriceIncrementRules rules=PriceIncrementRules.defaultRules();
    private final ProxyBidEngine engine=new ProxyBidEngine(rules);
    private final BidValidator validator=new BidValidator(rules);

    // CDI needs a no-argument constructor
    protected BiddingService()
    {
    }

    // Repositories and the notifier are injected by the container
    @Inject
    public BiddingService(ItemRepository items, BidRepository bids, AutoBidRepository autoBids,
                          UserRepository users, NotificationPort notifier)
    {
        this.items=items;
        this.bids=bids;
        this.autoBids=autoBids;
        this.users=users;
        this.notifier=notifier;
    }

    // Places a bid: the entered amount is the bidder's maximum
    @Transactional
    public BidOutcome placeBid(long itemId, long bidderId, BigDecimal amount)
    {
        return commitMaximum(itemId, bidderId, amount);
    }

    // Sets (or raises) an automatic proxy bid with the given hidden maximum
    @Transactional
    public BidOutcome placeAutoBid(long itemId, long bidderId, BigDecimal maxAmount)
    {
        return commitMaximum(itemId, bidderId, maxAmount);
    }

    // Shared path for both bid types: check the rules, store the ceiling, then re-resolve the auction
    private BidOutcome commitMaximum(long itemId, long bidderId, BigDecimal amount)
    {
        Item item=items.findByIdForUpdate(itemId)
                .orElseThrow(()->new ItemNotFoundException(itemId));
        User bidder=users.findById(bidderId)
                .orElseThrow(()->new UserNotFoundException(bidderId));
        Instant now=now();

        // A seller cannot bid on their own auction.
        if(item.getSeller()!=null&&item.getSeller().getId().equals(bidder.getId()))
        {
            throw new SellerCannotBidException(itemId);
        }

        // You can't outbid yourself: reject if you're already the highest bidder.
        if(item.getWinner()!=null&&item.getWinner().getId().equals(bidder.getId()))
        {
            throw new AlreadyHighestBidderException(itemId);
        }

        // Reject closed auctions and amounts below the minimum next bid.
        AuctionSnapshot snapshot=
                new AuctionSnapshot(item.getStatus(), item.getCurrentPrice(), item.getEndDate());
        validator.validate(snapshot, amount, now);

        // Record / raise this bidder's standing maximum (one per item, monotonic).
        upsertStandingMax(item, bidder, amount);

        return resolveAndApply(item, bidder, now);
    }

    // Stores the bidder's ceiling, one per item; an existing ceiling may only go up
    private void upsertStandingMax(Item item, User bidder, BigDecimal amount)
    {
        AutoBid existing=autoBids.findByItemAndBidder(item, bidder).orElse(null);
        if(existing==null)
        {
            autoBids.save(new AutoBid(item, bidder, amount));
        }
        else
        {
            // A bidder may only raise their ceiling.
            if(amount.compareTo(existing.getMaxAmount())>0)
            {
                existing.setMaxAmount(amount);
            }

            existing.setActive(true);
            autoBids.save(existing);
        }
    }

    // Asks the proxy engine who leads now, then saves the new price, leader and bid row
    private BidOutcome resolveAndApply(Item item, User actor, Instant now)
    {
        List<StandingBid> standing=autoBids.findActiveByItem(item).stream()
                .map(a->new StandingBid(a.getBidder().getId(), a.getMaxAmount(), a.getCreatedAt()))
                .toList();

        BidResolution resolution=engine.resolve(item.getStartPrice(), standing);
        BigDecimal newPrice=resolution.currentPrice();
        Long leaderId=resolution.winningBidderId();

        User leader=resolveLeader(actor, leaderId);

        // Log the resulting top offer; flagged auto if the system out-bid the actor.
        boolean auto=leader!=null&&!leader.getId().equals(actor.getId());
        bids.save(new Bid(item, leader, newPrice, auto));

        item.setCurrentPrice(newPrice);
        item.setWinner(leader);
        items.save(item); // optimistic @Version check happens on flush/commit

        notifier.bidPlaced(item.getId(), newPrice, leaderId);

        boolean actorLeading=leaderId!=null&&leaderId.equals(actor.getId());
        return new BidOutcome(newPrice, leaderId, actorLeading, item.getStatus());
    }

    // Turns the winning bidder id into a User
    private User resolveLeader(User actor, Long leaderId)
    {
        if(leaderId==null)
        {
            return null;
        }

        if(leaderId.equals(actor.getId()))
        {
            return actor;
        }

        return users.findById(leaderId)
                .orElseThrow(()->new UserNotFoundException(leaderId));
    }

    // Immediately purchases an item at its buy-now price, closing the auction
    @Transactional
    public BidOutcome buyNow(long itemId, long bidderId)
    {
        Item item=items.findByIdForUpdate(itemId)
                .orElseThrow(()->new ItemNotFoundException(itemId));
        User bidder=users.findById(bidderId)
                .orElseThrow(()->new UserNotFoundException(bidderId));
        Instant now=now();

        // A seller cannot buy their own item.
        if(item.getSeller()!=null&&item.getSeller().getId().equals(bidder.getId()))
        {
            throw new SellerCannotBidException(itemId);
        }

        if(item.getStatus()!=ItemStatus.ACTIVE||!now.isBefore(item.getEndDate()))
        {
            throw new AuctionClosedException("Auction is not open for buy-now (item "+itemId+")");
        }

        if(!item.hasBuyNow())
        {
            throw new BuyNowNotAvailableException(itemId);
        }

        BigDecimal price=item.getBuyNowPrice();
        item.setCurrentPrice(price);
        item.setWinner(bidder);
        item.setStatus(ItemStatus.SOLD);
        bids.save(new Bid(item, bidder, price, false));

        // Cancel any outstanding proxy bids.
        for(AutoBid a:autoBids.findActiveByItem(item))
        {
            a.setActive(false);
            autoBids.save(a);
        }

        items.save(item);

        notifier.auctionClosed(item.getId(), price, bidder.getId());
        return new BidOutcome(price, bidder.getId(), true, ItemStatus.SOLD);
    }

    // Clock in one place, so tests can freeze time
    protected Instant now()
    {
        return Instant.now();
    }
}
