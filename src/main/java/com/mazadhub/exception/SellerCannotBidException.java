package com.mazadhub.exception;

// Thrown when the seller of an item tries to bid on (or buy-now) their own auction
public class SellerCannotBidException extends RuntimeException
{
    public SellerCannotBidException(long itemId)
    {
        super("You cannot bid on your own item.");
    }
}
