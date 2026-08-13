package com.mazadhub.web.support;

import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import com.mazadhub.domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.util.List;

/**
 * Extra read-only queries the JSF screens need that aren't on the existing
 * repositories. Kept in one place so the original repository classes stay
 * untouched. Read-only, so no transaction is required.
 */
@ApplicationScoped
public class WebQueries {

    @PersistenceContext(unitName = "mazadhubPU")
    private EntityManager em;

    /** All live auctions, soonest-ending first (home catalogue default). */
    public List<Item> allActiveItems() {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.status = :st ORDER BY i.endDate ASC", Item.class)
                .setParameter("st", ItemStatus.ACTIVE)
                .getResultList();
    }

    /** Items a given user has listed for sale (their auctions). */
    public List<Item> itemsBySeller(long sellerId) {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.seller.id = :sid ORDER BY i.endDate DESC", Item.class)
                .setParameter("sid", sellerId)
                .getResultList();
    }

    /** Distinct items a given user has placed at least one bid on. */
    public List<Item> itemsBidByUser(long bidderId) {
        return em.createQuery(
                        "SELECT DISTINCT b.item FROM Bid b WHERE b.bidder.id = :uid", Item.class)
                .setParameter("uid", bidderId)
                .getResultList();
    }

    /** The highest amount this user has bid on this item (null if none). */
    public BigDecimal highestBid(long itemId, long bidderId) {
        return em.createQuery(
                        "SELECT MAX(b.amount) FROM Bid b WHERE b.item.id = :iid AND b.bidder.id = :uid",
                        BigDecimal.class)
                .setParameter("iid", itemId)
                .setParameter("uid", bidderId)
                .getSingleResult();
    }

    /** All users, for the admin panel. */
    public List<User> allUsers() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.username", User.class)
                .getResultList();
    }

    /** How many items are in a category, for the admin panel. */
    public long itemCountByCategory(long categoryId) {
        return em.createQuery(
                        "SELECT COUNT(i) FROM Item i WHERE i.category.id = :cid", Long.class)
                .setParameter("cid", categoryId)
                .getSingleResult();
    }
}
