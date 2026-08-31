package com.mazadhub.web;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.service.ItemService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.math.BigDecimal;
import java.util.List;

// Backs the "list a new item for sale" form
@Named
@RequestScoped
public class SellItemBean
{
    // creates the item, injected by the container
    @Inject
    private ItemService items;

    // tells us who the seller is
    @Inject
    private SessionBean session;

    private List<Category> categories;

    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal startPrice;
    private BigDecimal buyNowPrice; // optional
    private int durationDays=7;
    private String imageUrl;

    // Loads the category drop-down when the bean is created
    @PostConstruct
    public void init()
    {
        categories=items.listCategories();
    }

    // Sends a visitor to the login page before the form is shown
    public String guard()
    {
        return session.isLoggedIn()?null:"login?faces-redirect=true";
    }

    // Creates the auction and jumps to its item page, or shows what went wrong
    public String publish()
    {
        if(!session.isLoggedIn())
        {
            return "login?faces-redirect=true";
        }

        try
        {
            Item created=items.listForSale(session.getUserId(), categoryId, title, description,
                    startPrice, buyNowPrice, durationDays, imageUrl);
            return "item?faces-redirect=true&id="+created.getId();
        }
        catch(RuntimeException e)
        {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            e.getMessage()!=null?e.getMessage():"Could not list the item.", null));
            return null;
        }
    }

    // getters / setters used by the JSF pages and services
    public List<Category> getCategories()
    {
        return categories;
    }

    public Long getCategoryId()
    {
        return categoryId;
    }

    public void setCategoryId(Long v)
    {
        this.categoryId=v;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String v)
    {
        this.title=v;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String v)
    {
        this.description=v;
    }

    public BigDecimal getStartPrice()
    {
        return startPrice;
    }

    public void setStartPrice(BigDecimal v)
    {
        this.startPrice=v;
    }

    public BigDecimal getBuyNowPrice()
    {
        return buyNowPrice;
    }

    public void setBuyNowPrice(BigDecimal v)
    {
        this.buyNowPrice=v;
    }

    public int getDurationDays()
    {
        return durationDays;
    }

    public void setDurationDays(int v)
    {
        this.durationDays=v;
    }

    public String getImageUrl()
    {
        return imageUrl;
    }

    public void setImageUrl(String v)
    {
        this.imageUrl=v;
    }
}
