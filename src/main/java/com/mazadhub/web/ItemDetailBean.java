package com.mazadhub.web;

import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Item;
import com.mazadhub.pricing.PriceIncrementRules;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.service.BiddingService;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.util.List;

/**
 * Item details + bid history + the three bidding actions (bid, proxy bid,
 * buy-now). The item id comes from the {@code id} view parameter in the URL.
 */
@Named
@RequestScoped
public class ItemDetailBean {

    private static final PriceIncrementRules RULES = PriceIncrementRules.defaultRules();

    @Inject
    private ItemService items;

    @Inject
    private BiddingService bidding;

    @Inject
    private BidRepository bids;

    @Inject
    private SessionBean session;

    private Long itemId;
    private Item item;
    private List<Bid> history;
    private long bidCount;

    // form inputs
    private BigDecimal bidAmount;
    private BigDecimal autoMax;

    /** f:viewAction target — loads everything for the current id. */
    public void load() {
        if (itemId == null) {
            return;
        }
        item = items.getById(itemId);
        history = bids.findByItemOrderByAmountDesc(item);
        bidCount = bids.countByItem(item);
    }

    public BigDecimal getMinNextBid() {
        return item == null ? null : RULES.minNextBid(item.getCurrentPrice());
    }

    public String placeBid() {
        if (guardGuest()) {
            return null;
        }
        try {
            bidding.placeBid(itemId, session.getUserId(), bidAmount);
            info("Bid placed.");
        } catch (RuntimeException e) {
            error(friendly(e));
        }
        return reload();
    }

    public String placeAutoBid() {
        if (guardGuest()) {
            return null;
        }
        try {
            bidding.placeAutoBid(itemId, session.getUserId(), autoMax);
            info("Automatic bidding is set.");
        } catch (RuntimeException e) {
            error(friendly(e));
        }
        return reload();
    }

    public String buyNow() {
        if (guardGuest()) {
            return null;
        }
        try {
            bidding.buyNow(itemId, session.getUserId());
            info("Purchase complete — the auction is closed.");
        } catch (RuntimeException e) {
            error(friendly(e));
        }
        return reload();
    }

    private boolean guardGuest() {
        if (!session.isLoggedIn()) {
            error("Please log in to bid.");
            return true;
        }
        return false;
    }

    private String reload() {
        return "item?faces-redirect=true&id=" + itemId;
    }

    private String friendly(RuntimeException e) {
        return e.getMessage() != null ? e.getMessage() : "Action could not be completed.";
    }

    private void info(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, m, null));
    }

    private void error(String m) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, m, null));
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long v) { this.itemId = v; }
    public Item getItem() { return item; }
    public List<Bid> getHistory() { return history; }
    public long getBidCount() { return bidCount; }
    public BigDecimal getBidAmount() { return bidAmount; }
    public void setBidAmount(BigDecimal v) { this.bidAmount = v; }
    public BigDecimal getAutoMax() { return autoMax; }
    public void setAutoMax(BigDecimal v) { this.autoMax = v; }
}
