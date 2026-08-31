package com.mazadhub.web;

import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Item;
import com.mazadhub.pricing.PriceIncrementRules;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.service.BiddingService;
import com.mazadhub.service.ItemService;
import jakarta.faces.view.ViewScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

// Item page: details, bid history and the three actions (bid, automatic bid, buy now)
// View scoped so the form still knows the item when the page posts back
@Named
@ViewScoped
public class ItemDetailBean implements Serializable
{
    private static final long serialVersionUID=1L;

    // used to show the minimum next bid
    private static final PriceIncrementRules RULES=PriceIncrementRules.defaultRules();

    // services and repository the page reads through, injected by the container
    @Inject
    private ItemService items;

    @Inject
    private BiddingService bidding;

    @Inject
    private BidRepository bids;

    @Inject
    private SessionBean session;

    // the item being viewed, taken from the id in the URL
    private Long itemId;
    private Item item;
    private List<Bid> history;
    private long bidCount;

    // form inputs
    private BigDecimal bidAmount;
    private BigDecimal autoMax;

    // Reads the item and its bid history; also re-run after every action
    public void load()
    {
        if(itemId==null)
        {
            return;
        }

        item=items.getById(itemId);
        history=bids.findByItemOrderByAmountDesc(item);
        bidCount=bids.countByItem(item);
    }

    // The smallest amount the next bid may be
    public BigDecimal getMinNextBid()
    {
        return item==null?null:RULES.minNextBid(item.getCurrentPrice());
    }

    // Places the typed amount as a bid, then refreshes the page data
    public String placeBid()
    {
        if(guardGuest())
        {
            return null;
        }

        try
        {
            bidding.placeBid(itemId, session.getUserId(), bidAmount);
            bidAmount=null;
            info("Bid placed.");
        }
        catch(RuntimeException e)
        {
            error(friendly(e));
        }

        load();
        return null;
    }

    // Stores the typed ceiling as an automatic bid, then refreshes the page data
    public String placeAutoBid()
    {
        if(guardGuest())
        {
            return null;
        }

        try
        {
            bidding.placeAutoBid(itemId, session.getUserId(), autoMax);
            autoMax=null;
            info("Automatic bidding is set.");
        }
        catch(RuntimeException e)
        {
            error(friendly(e));
        }

        load();
        return null;
    }

    // Buys the item at its buy-now price and closes the auction
    public String buyNow()
    {
        if(guardGuest())
        {
            return null;
        }

        try
        {
            bidding.buyNow(itemId, session.getUserId());
            info("Purchase complete — the auction is closed.");
        }
        catch(RuntimeException e)
        {
            error(friendly(e));
        }

        load();
        return null;
    }

    // Blocks the action and asks a visitor to log in first
    private boolean guardGuest()
    {
        if(!session.isLoggedIn())
        {
            error("Please log in to bid.");
            return true;
        }

        return false;
    }

    // The message to show the user when an action fails
    private String friendly(RuntimeException e)
    {
        return e.getMessage()!=null?e.getMessage():"Action could not be completed.";
    }

    // Shows a green message on the page
    private void info(String m)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    // Shows a red message on the page
    private void error(String m)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    // getters / setters used by the JSF pages and services
    public Long getItemId()
    {
        return itemId;
    }

    public void setItemId(Long v)
    {
        this.itemId=v;
    }

    public Item getItem()
    {
        return item;
    }

    public List<Bid> getHistory()
    {
        return history;
    }

    public long getBidCount()
    {
        return bidCount;
    }

    public BigDecimal getBidAmount()
    {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal v)
    {
        this.bidAmount=v;
    }

    public BigDecimal getAutoMax()
    {
        return autoMax;
    }

    public void setAutoMax(BigDecimal v)
    {
        this.autoMax=v;
    }
}
