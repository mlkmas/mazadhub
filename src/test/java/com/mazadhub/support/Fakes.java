package com.mazadhub.support;

import com.mazadhub.domain.AutoBid;
import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.notification.NotificationPort;
import com.mazadhub.repository.AutoBidRepository;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.repository.ItemRepository;
import com.mazadhub.repository.UserRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// In-memory stand-ins for the repositories and the notifier, so the services can be tested without a database
public final class Fakes
{
    // Container class only, never instantiated
    private Fakes()
    {
    }

    // User repository backed by a map
    public static final class Users extends UserRepository
    {
        private final Map<Long, User> store=new HashMap<>();
        private long seq=0;

        @Override
        public Optional<User> findById(Long id)
        {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public User save(User u)
        {
            if(u.getId()==null)
            {
                TestIds.withId(u,++seq);
            }

            store.put(u.getId(), u);
            return u;
        }

        @Override
        public Optional<User> findByUsername(String username)
        {
            return store.values().stream()
                    .filter(u->username.equals(u.getUsername())).findFirst();
        }

        @Override
        public boolean existsByUsername(String username)
        {
            return findByUsername(username).isPresent();
        }
    }

    // Item repository backed by a map; the locked read is just a plain read here
    public static final class Items extends ItemRepository
    {
        private final Map<Long, Item> store=new HashMap<>();
        private long seq=0;

        @Override
        public Optional<Item> findById(Long id)
        {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public Optional<Item> findByIdForUpdate(Long id)
        {
            return findById(id);
        }

        @Override
        public Item save(Item item)
        {
            if(item.getId()==null)
            {
                TestIds.withId(item,++seq);
            }

            store.put(item.getId(), item);
            return item;
        }
    }

    // Bid repository that keeps every saved row, so tests can inspect the history
    public static final class Bids extends BidRepository
    {
        public final List<Bid> saved=new ArrayList<>();
        private long seq=0;

        @Override
        public Bid save(Bid bid)
        {
            if(bid.getId()==null)
            {
                TestIds.withId(bid,++seq);
            }

            saved.add(bid);
            return bid;
        }
    }

    // Proxy-bid repository backed by a list
    public static final class AutoBids extends AutoBidRepository
    {
        private final List<AutoBid> store=new ArrayList<>();
        private long seq=0;

        @Override
        public AutoBid save(AutoBid a)
        {
            if(a.getId()==null)
            {
                TestIds.withId(a,++seq);
                store.add(a);
            }

            return a;
        }

        @Override
        public Optional<AutoBid> findByItemAndBidder(Item item, User bidder)
        {
            return store.stream()
                    .filter(a->a.getItem().equals(item)&&a.getBidder().equals(bidder))
                    .findFirst();
        }

        @Override
        public List<AutoBid> findActiveByItem(Item item)
        {
            return store.stream()
                    .filter(a->a.getItem().equals(item)&&a.isActive())
                    .toList();
        }
    }

    // Notifier that counts the calls instead of publishing anything
    public static final class Notifier implements NotificationPort
    {
        public int bidPlacedCalls=0;
        public int auctionClosedCalls=0;
        public Long lastLeaderId;
        public BigDecimal lastPrice;

        @Override
        public void bidPlaced(long itemId, BigDecimal currentPrice, Long leaderId)
        {
            bidPlacedCalls++;
            lastPrice=currentPrice;
            lastLeaderId=leaderId;
        }

        @Override
        public void auctionClosed(long itemId, BigDecimal finalPrice, Long winnerId)
        {
            auctionClosedCalls++;
            lastPrice=finalPrice;
            lastLeaderId=winnerId;
        }
    }
}
