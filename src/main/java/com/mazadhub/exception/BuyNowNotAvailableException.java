package com.mazadhub.exception;

/** Thrown when buy-now is requested on an item that has no buy-now price. */
public class BuyNowNotAvailableException extends RuntimeException {
    public BuyNowNotAvailableException(long itemId) {
        super("Buy-now is not available for item " + itemId);
    }
}
