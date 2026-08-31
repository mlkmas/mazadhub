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

// Personal area: the auctions I listed and the auctions I bid on
@Named
@RequestScoped
public class MyAccountBean
{
    // extra read-only queries the screen needs, injected by the container
    @Inject
    private WebQueries queries;

    @Inject
    private BidRepository bids;

    @Inject
    private SessionBean session;

    private List<Item> myItems;
    private List<Item> myBids;

    // Loads both tables, or sends a visitor to the login page
    public String load()
    {
        if(!session.isLoggedIn())
        {
            return "login?faces-redirect=true";
        }

        myItems=queries.itemsBySeller(session.getUserId());
        myBids=queries.itemsBidByUser(session.getUserId());
        return null;
    }

    // How many bids one of my items has drawn
    public long bidCount(Item item)
    {
        return bids.countByItem(item);
    }

    // The highest amount I bid on an item
    public BigDecimal myBid(Item item)
    {
        return queries.highestBid(item.getId(), session.getUserId());
    }

    // Leading / Outbid / Won / Closed label for one of my bids
    public String bidStatus(Item item)
    {
        if(item.getStatus()==ItemStatus.SOLD)
        {
            return item.getWinner()!=null
                    &&item.getWinner().getId().equals(session.getUserId())?"Won":"Closed";
        }

        BigDecimal mine=queries.highestBid(item.getId(), session.getUserId());
        if(mine!=null&&mine.compareTo(item.getCurrentPrice())>=0)
        {
            return "Leading";
        }

        return "Outbid";
    }

    // getters / setters used by the JSF pages and services
    public List<Item> getMyItems()
    {
        return myItems;
    }

    public List<Item> getMyBids()
    {
        return myBids;
    }
}
