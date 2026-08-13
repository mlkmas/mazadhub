package com.mazadhub.web;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.service.ItemService;
import com.mazadhub.web.support.WebQueries;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

/**
 * Home / catalogue screen: category list on the side, item grid in the middle,
 * plus keyword search. Reads optional {@code category} and {@code q} view
 * parameters from the URL.
 */
@Named
@RequestScoped
public class CatalogBean {

    @Inject
    private ItemService items;

    @Inject
    private BidRepository bids;

    @Inject
    private WebQueries queries;

    private List<Category> categories;
    private List<Item> results;
    private Long selectedCategoryId;
    private String keyword;

    @PostConstruct
    public void init() {
        categories = items.listCategories();
    }

    /** Runs from the page's f:viewAction after the view params are set. */
    public void load() {
        if (keyword != null && !keyword.isBlank()) {
            results = items.search(keyword);
        } else if (selectedCategoryId != null) {
            results = items.browseByCategory(selectedCategoryId);
        } else {
            results = queries.allActiveItems();
        }
    }

    /** Bid count shown on each tile. */
    public long bidCount(Item item) {
        return bids.countByItem(item);
    }

    public List<Category> getCategories() { return categories; }
    public List<Item> getResults() { return results; }
    public Long getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(Long v) { this.selectedCategoryId = v; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String v) { this.keyword = v; }
}
