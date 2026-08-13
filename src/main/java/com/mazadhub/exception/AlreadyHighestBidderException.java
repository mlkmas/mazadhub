package com.mazadhub.exception;

/**
 * Thrown when a user tries to bid on an item they are already winning — you
 * can't outbid yourself. Raising your own hidden maximum while alone would only
 * add confusing no-op entries to the bid history without moving the price.
 */
public class AlreadyHighestBidderException extends RuntimeException {

    public AlreadyHighestBidderException(long itemId) {
        super("You are already the highest bidder on this item.");
    }
}
