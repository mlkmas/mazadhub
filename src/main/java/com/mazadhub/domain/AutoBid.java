package com.mazadhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A bidder's standing proxy bid on an item: the hidden maximum the system may
 * bid up to on their behalf. At most one active auto-bid exists per
 * (item, bidder) pair.
 */
@Entity
@Table(name = "auto_bids",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_autobid_item_bidder", columnNames = {"item_id", "bidder_id"}),
        indexes = @Index(name = "idx_autobids_item", columnList = "item_id"))
public class AutoBid implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "autobid_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "max_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "active", nullable = false)
    private boolean active = true;

    protected AutoBid() {
        // required by JPA
    }

    public AutoBid(Item item, User bidder, BigDecimal maxAmount) {
        this.item = item;
        this.bidder = bidder;
        this.maxAmount = maxAmount;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public User getBidder() {
        return bidder;
    }

    public BigDecimal getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(BigDecimal maxAmount) {
        this.maxAmount = maxAmount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AutoBid other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
