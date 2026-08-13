package com.mazadhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An item put up for auction. Holds the live auction state (current price,
 * status, winner) that the bidding service updates as bids arrive.
 *
 * <p>The {@link #version} field enables optimistic locking: if two concurrent
 * transactions try to update the same item (e.g. two simultaneous bids), the
 * second commit fails with an {@code OptimisticLockException} and is retried,
 * guaranteeing a consistent current price under contention.
 */
@Entity
@Table(name = "items", indexes = {
        @Index(name = "idx_items_category", columnList = "category_id"),
        @Index(name = "idx_items_status_end", columnList = "status,end_date")
})
public class Item implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** The winning bidder, set when the auction closes with a sale. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @Column(name = "title", nullable = false, length = 150)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "start_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal startPrice;

    @Column(name = "current_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(name = "buy_now_price", precision = 12, scale = 2)
    private BigDecimal buyNowPrice;

    @Column(name = "start_date", nullable = false)
    private Instant startDate = Instant.now();

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ItemStatus status = ItemStatus.ACTIVE;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    protected Item() {
        // required by JPA
    }

    public Item(User seller, Category category, String title,
                BigDecimal startPrice, Instant endDate) {
        this.seller = seller;
        this.category = category;
        this.title = title;
        this.startPrice = startPrice;
        this.currentPrice = startPrice;
        this.endDate = endDate;
    }

    @Override
    public Long getId() {
        return id;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getWinner() {
        return winner;
    }

    public void setWinner(User winner) {
        this.winner = winner;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public BigDecimal getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(BigDecimal startPrice) {
        this.startPrice = startPrice;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    /** True if buy-now is offered for this item. */
    public boolean hasBuyNow() {
        return buyNowPrice != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
