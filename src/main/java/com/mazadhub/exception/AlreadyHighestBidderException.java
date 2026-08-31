package com.mazadhub.exception;

// Thrown when a user tries to bid on an item they are already winning — you can't outbid yourself
public class AlreadyHighestBidderException extends RuntimeException
{
    public AlreadyHighestBidderException(long itemId)
    {
        super("You are already the highest bidder on this item.");
    }
}
