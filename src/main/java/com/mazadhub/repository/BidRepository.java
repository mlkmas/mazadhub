package com.mazadhub.repository;

import com.mazadhub.domain.Bid;
import com.mazadhub.domain.Item;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Data-access operations for {@link Bid}.
 */
@ApplicationScoped
public class BidRepository extends AbstractRepository<Bid> {

    public BidRepository() {
        super(Bid.class);
    }

    /** All bids on an item, highest amount first (bid history view). */
    public List<Bid> findByItemOrderByAmountDesc(Item item) {
        return em.createQuery(
                        "SELECT b FROM Bid b WHERE b.item = :item ORDER BY b.amount DESC, b.bidTime ASC",
                        Bid.class)
                .setParameter("item", item)
                .getResultList();
    }

    public long countByItem(Item item) {
        return em.createQuery("SELECT COUNT(b) FROM Bid b WHERE b.item = :item", Long.class)
                .setParameter("item", item)
                .getSingleResult();
    }
}
