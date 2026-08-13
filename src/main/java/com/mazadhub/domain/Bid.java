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

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A single bid placed on an item. {@code auto = true} marks a bid that was
 * generated automatically by the proxy-bidding engine on a bidder's behalf.
 */
@Entity
@Table(name = "bids", indexes = {
        @Index(name = "idx_bids_item", columnList = "item_id"),
        @Index(name = "idx_bids_bidder", columnList = "bidder_id")
})
public class Bid implements Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bid_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "bid_time", nullable = false)
    private Instant bidTime = Instant.now();

    @Column(name = "is_auto", nullable = false)
    private boolean auto;

    protected Bid() {
        // required by JPA
    }

    public Bid(Item item, User bidder, BigDecimal amount, boolean auto) {
        this.item = item;
        this.bidder = bidder;
        this.amount = amount;
        this.auto = auto;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getBidTime() {
        return bidTime;
    }

    public boolean isAuto() {
        return auto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bid other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
