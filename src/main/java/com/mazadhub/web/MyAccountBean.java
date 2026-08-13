package com.mazadhub.web;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.web.support.WebQueries;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.util.List;

/** Personal area: "My items" (auctions I listed) and "My bids". */
@Named
@RequestScoped
public class MyAccountBean {

    @Inject
    private WebQueries queries;

    @Inject
    private BidRepository bids;

    @Inject
    private SessionBean session;

    private List<Item> myItems;
    private List<Item> myBids;

    public String load() {
        if (!session.isLoggedIn()) {
            return "login?faces-redirect=true";
        }
        myItems = queries.itemsBySeller(session.getUserId());
        myBids = queries.itemsBidByUser(session.getUserId());
        return null;
    }

    /** Number of bids on one of my items. */
    public long bidCount(Item item) {
        return bids.countByItem(item);
    }

    /** My highest bid on an item I participated in. */
    public BigDecimal myBid(Item item) {
        return queries.highestBid(item.getId(), session.getUserId());
    }

    /** Simple status label for one of my bids. */
    public String bidStatus(Item item) {
        if (item.getStatus() == ItemStatus.SOLD) {
            return item.getWinner() != null
                    && item.getWinner().getId().equals(session.getUserId()) ? "Won" : "Closed";
        }
        BigDecimal mine = queries.highestBid(item.getId(), session.getUserId());
        if (mine != null && mine.compareTo(item.getCurrentPrice()) >= 0) {
            return "Leading";
        }
        return "Outbid";
    }

    public List<Item> getMyItems() { return myItems; }
    public List<Item> getMyBids() { return myBids; }
}
