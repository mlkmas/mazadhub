package com.mazadhub.web;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * Auction result for the seller. The winner's contact details are shown only
 * when the item is SOLD, and only to the seller who listed it.
 */
@Named
@RequestScoped
public class SellerResultBean {

    @Inject
    private ItemService items;

    @Inject
    private SessionBean session;

    private Long itemId;
    private Item item;

    public String load() {
        if (!session.isLoggedIn()) {
            return "login?faces-redirect=true";
        }
        item = items.getById(itemId);
        // Only the seller may see the result.
        if (item.getSeller() == null || !item.getSeller().getId().equals(session.getUserId())) {
            return "catalog?faces-redirect=true";
        }
        return null;
    }

    public boolean isSold() {
        return item != null && item.getStatus() == ItemStatus.SOLD && item.getWinner() != null;
    }

    public Long getItemId() { return itemId; }
    public void setItemId(Long v) { this.itemId = v; }
    public Item getItem() { return item; }
}
