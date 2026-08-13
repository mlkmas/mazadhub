package com.mazadhub.repository;

import com.mazadhub.domain.Category;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.ItemStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Data-access operations for {@link Item}, including the locked lookup used
 * when a bid mutates an item's current price.
 */
@ApplicationScoped
public class ItemRepository extends AbstractRepository<Item> {

    public ItemRepository() {
        super(Item.class);
    }

    /**
     * Loads an item with a pessimistic write lock, so a bidding transaction can
     * read-then-update the current price without another transaction racing it.
     * Used together with the entity's {@code @Version} field for safe concurrent
     * bidding.
     */
    public Optional<Item> findByIdForUpdate(Long id) {
        return Optional.ofNullable(em.find(Item.class, id, LockModeType.PESSIMISTIC_WRITE));
    }

    /** Active items in a category, newest first. */
    public List<Item> findActiveByCategory(Category category) {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.category = :category " +
                                "AND i.status = :status ORDER BY i.startDate DESC", Item.class)
                .setParameter("category", category)
                .setParameter("status", ItemStatus.ACTIVE)
                .getResultList();
    }

    /** Active items whose title contains the keyword (case-insensitive). */
    public List<Item> searchActiveByKeyword(String keyword) {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.status = :status " +
                                "AND LOWER(i.title) LIKE :kw ORDER BY i.endDate ASC", Item.class)
                .setParameter("status", ItemStatus.ACTIVE)
                .setParameter("kw", "%" + keyword.toLowerCase() + "%")
                .getResultList();
    }

    /**
     * Active auctions whose end time has passed — the work list for the
     * scheduler that closes expired auctions.
     */
    public List<Item> findExpiredActive(Instant now) {
        return em.createQuery(
                        "SELECT i FROM Item i WHERE i.status = :status AND i.endDate <= :now",
                        Item.class)
                .setParameter("status", ItemStatus.ACTIVE)
                .setParameter("now", now)
                .getResultList();
    }
}
