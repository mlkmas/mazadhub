package com.mazadhub.api;

import com.mazadhub.domain.Item;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

/**
 * Tiny endpoint the item page polls for live price updates:
 * {@code GET /api/live/{itemId}} → current price, bid count and status.
 *
 * <p>Kept separate from {@link ItemResource} because it is called frequently and
 * returns the smallest possible payload.
 */
@Path("live")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class LivePriceResource {

    @Inject
    private ItemService items;

    @Inject
    private BidRepository bids;

    @GET
    @Path("{itemId}")
    @Transactional
    public Map<String, Object> live(@PathParam("itemId") long itemId) {
        Item item = items.getById(itemId);
        return Map.of(
                "itemId", item.getId(),
                "price", item.getCurrentPrice().toPlainString(),
                "bidCount", bids.countByItem(item),
                "status", item.getStatus().name());
    }
}
