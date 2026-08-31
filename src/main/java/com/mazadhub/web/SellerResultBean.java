package com.mazadhub.web;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// Result screen a seller sees once their auction has finished
@Named
@RequestScoped
public class SellerResultBean
{
    // reads the finished item, injected by the container
    @Inject
    private ItemService items;

    @Inject
    private SessionBean session;

    private Long itemId;
    private Item item;

    // Loads the item and turns away anyone who is not its seller
    public String load()
    {
        if(!session.isLoggedIn())
        {
            return "login?faces-redirect=true";
        }

        item=items.getById(itemId);
        // Only the seller may see the result.
        if(item.getSeller()==null||!item.getSeller().getId().equals(session.getUserId()))
        {
            return "catalog?faces-redirect=true";
        }

        return null;
    }

    // True when the auction ended with a winner
    public boolean isSold()
    {
        return item!=null&&item.getStatus()==ItemStatus.SOLD&&item.getWinner()!=null;
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
}
