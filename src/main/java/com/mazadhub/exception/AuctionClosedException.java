package com.mazadhub.exception;

// Thrown when a bid is attempted on an auction that isn't currently accepting bids –either because it is not ACTIVE or because its end time has passed
public class AuctionClosedException extends RuntimeException
{
    public AuctionClosedException(String message)
    {
        super(message);
    }
}
