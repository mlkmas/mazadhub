package com.mazadhub.api;

import com.mazadhub.api.dto.AutoBidRequest;
import com.mazadhub.api.dto.BidDTO;
import com.mazadhub.api.dto.BidOutcomeDTO;
import com.mazadhub.api.dto.BuyNowRequest;
import com.mazadhub.api.dto.ItemDetailDTO;
import com.mazadhub.api.dto.ItemSummaryDTO;
import com.mazadhub.api.dto.ListItemRequest;
import com.mazadhub.api.dto.PlaceBidRequest;
import com.mazadhub.api.dto.SellerResultDTO;
import com.mazadhub.domain.Item;
import com.mazadhub.repository.BidRepository;
import com.mazadhub.service.BiddingService;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

/**
 * The main resource: browsing and searching the catalogue, item details and
 * bid history, listing a new item, and the three bidding actions (bid, proxy
 * bid, buy-now). Each method is a thin adapter — it delegates to the Day 3
 * services and maps the result to a DTO. Read methods are {@code @Transactional}
 * so lazy associations (category, winner) are still loaded while the DTO is
 * built.
 */
@Path("items")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ItemResource {

    private ItemService items;
    private BiddingService bidding;
    private BidRepository bids;

    protected ItemResource() {
        // for the CDI proxy
    }

    @Inject
    public ItemResource(ItemService items, BiddingService bidding, BidRepository bids) {
        this.items = items;
        this.bidding = bidding;
        this.bids = bids;
    }

    /**
     * Browse or search active items.
     * {@code GET /api/items?search=canon} or {@code GET /api/items?category=3}.
     */
    @GET
    @Transactional
    public List<ItemSummaryDTO> browse(@QueryParam("category") Long categoryId,
                                       @QueryParam("search") String search) {
        List<Item> found;
        if (search != null && !search.isBlank()) {
            found = items.search(search);
        } else if (categoryId != null) {
            found = items.browseByCategory(categoryId);
        } else {
            found = List.of();
        }
        return found.stream()
                .map(i -> ItemSummaryDTO.from(i, bids.countByItem(i)))
                .toList();
    }

    /** {@code GET /api/items/{id}} — full item details. */
    @GET
    @Path("{id}")
    @Transactional
    public ItemDetailDTO details(@PathParam("id") long id) {
        Item item = items.getById(id);
        return ItemDetailDTO.from(item, bids.countByItem(item));
    }

    /** {@code GET /api/items/{id}/bids} — anonymous bid history, highest first. */
    @GET
    @Path("{id}/bids")
    @Transactional
    public List<BidDTO> history(@PathParam("id") long id) {
        Item item = items.getById(id);
        return bids.findByItemOrderByAmountDesc(item).stream().map(BidDTO::from).toList();
    }

    /** {@code GET /api/items/{id}/result} — seller-facing result (winner revealed only if SOLD). */
    @GET
    @Path("{id}/result")
    @Transactional
    public SellerResultDTO result(@PathParam("id") long id) {
        return SellerResultDTO.from(items.getById(id));
    }

    /** {@code POST /api/items} — list a new item for sale. */
    @POST
    public Response list(ListItemRequest req) {
        Item item = items.listForSale(req.sellerId(), req.categoryId(), req.title(),
                req.description(), req.startPrice(), req.buyNowPrice(),
                req.durationDays(), req.imageUrl());
        return Response.status(Response.Status.CREATED)
                .entity(ItemDetailDTO.from(item, 0))
                .build();
    }

    /** {@code POST /api/items/{id}/bids} — place a bid (the amount is the bidder's maximum). */
    @POST
    @Path("{id}/bids")
    public BidOutcomeDTO placeBid(@PathParam("id") long id, PlaceBidRequest req) {
        return BidOutcomeDTO.from(bidding.placeBid(id, req.bidderId(), req.amount()));
    }

    /** {@code POST /api/items/{id}/autobid} — set / raise a hidden proxy ceiling. */
    @POST
    @Path("{id}/autobid")
    public BidOutcomeDTO autoBid(@PathParam("id") long id, AutoBidRequest req) {
        return BidOutcomeDTO.from(bidding.placeAutoBid(id, req.bidderId(), req.maxAmount()));
    }

    /** {@code POST /api/items/{id}/buy-now} — buy immediately, closing the auction. */
    @POST
    @Path("{id}/buy-now")
    public BidOutcomeDTO buyNow(@PathParam("id") long id, BuyNowRequest req) {
        return BidOutcomeDTO.from(bidding.buyNow(id, req.bidderId()));
    }
}
