package com.mazadhub.service;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import com.mazadhub.exception.ItemNotFoundException;
import com.mazadhub.exception.UserNotFoundException;
import com.mazadhub.repository.CategoryRepository;
import com.mazadhub.repository.ItemRepository;
import com.mazadhub.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Listing items for sale and browsing / searching the catalogue.
 */
@ApplicationScoped
public class ItemService {

    private ItemRepository items;
    private CategoryRepository categories;
    private UserRepository users;

    protected ItemService() {
        // for the CDI proxy
    }

    @Inject
    public ItemService(ItemRepository items, CategoryRepository categories, UserRepository users) {
        this.items = items;
        this.categories = categories;
        this.users = users;
    }

    /**
     * Lists a new item for sale, running for {@code durationDays} from now.
     */
    @Transactional
    public Item listForSale(long sellerId, long categoryId, String title, String description,
                            BigDecimal startPrice, BigDecimal buyNowPrice,
                            int durationDays, String imageUrl) {
        User seller = users.findById(sellerId)
                .orElseThrow(() -> new UserNotFoundException(sellerId));
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + categoryId));
        if (durationDays <= 0) {
            throw new IllegalArgumentException("durationDays must be positive");
        }
        if (startPrice == null || startPrice.signum() < 0) {
            throw new IllegalArgumentException("startPrice must be >= 0");
        }

        Instant end = now().plus(Duration.ofDays(durationDays));
        Item item = new Item(seller, category, title, startPrice, end);
        item.setDescription(description);
        item.setImageUrl(imageUrl);
        item.setBuyNowPrice(buyNowPrice);
        return items.save(item);
    }

    public Item getById(long itemId) {
        return items.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    public List<Category> listCategories() {
        return categories.findAll();
    }

    public List<Item> browseByCategory(long categoryId) {
        Category category = categories.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException("Category not found: " + categoryId));
        return items.findActiveByCategory(category);
    }

    public List<Item> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        return items.searchActiveByKeyword(keyword.trim());
    }

    /** Overridable clock seam for testing. */
    protected Instant now() {
        return Instant.now();
    }
}
