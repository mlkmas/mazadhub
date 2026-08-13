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

/**
 * Item details + bid history + the three bidding actions (bid, proxy bid,
 * buy-now).
 *
 * <p>This bean is {@code @ViewScoped}: the same instance is kept for as long as
 * the user stays on the item page, so {@code item} (and {@code itemId}) survive
 * across form posts. That matters because the bid form is only rendered when
 * {@code item} is present — with a request-scoped bean the item would be null on
 * postback and JSF would skip the button action.
 */
@Named
@ViewScoped
public class ItemDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

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

    /** f:viewAction target on first load; also re-run after each action. */
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
            bidAmount = null;
            info("Bid placed.");
        } catch (RuntimeException e) {
            error(friendly(e));
        }
        load();
        return null;
    }

    public String placeAutoBid() {
        if (guardGuest()) {
            return null;
        }
        try {
            bidding.placeAutoBid(itemId, session.getUserId(), autoMax);
            autoMax = null;
            info("Automatic bidding is set.");
        } catch (RuntimeException e) {
            error(friendly(e));
        }
        load();
        return null;
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
        load();
        return null;
    }

    private boolean guardGuest() {
        if (!session.isLoggedIn()) {
            error("Please log in to bid.");
            return true;
        }
        return false;
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
