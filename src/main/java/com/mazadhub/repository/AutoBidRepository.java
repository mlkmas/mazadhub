package com.mazadhub.repository;

import com.mazadhub.domain.AutoBid;
import com.mazadhub.domain.Item;
import com.mazadhub.domain.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;

// Data-access operations for AutoBid (proxy bids)
@ApplicationScoped
public class AutoBidRepository extends AbstractRepository<AutoBid>
{
    // Tells the base class which entity this repository manages
    public AutoBidRepository()
    {
        super(AutoBid.class);
    }

    // All active proxy bids on an item — the input to the proxy-bid engine
    public List<AutoBid> findActiveByItem(Item item)
    {
        return em.createQuery(
                        "SELECT a FROM AutoBid a WHERE a.item = :item AND a.active = true",
                        AutoBid.class)
                .setParameter("item", item)
                .getResultList();
    }

    // The (single) proxy bid for a given bidder on a given item, if any
    public Optional<AutoBid> findByItemAndBidder(Item item, User bidder)
    {
        return em.createQuery(
                        "SELECT a FROM AutoBid a WHERE a.item = :item AND a.bidder = :bidder",
                        AutoBid.class)
                .setParameter("item", item)
                .setParameter("bidder", bidder)
                .getResultStream()
                .findFirst();
    }
}
