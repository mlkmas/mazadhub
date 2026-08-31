package com.mazadhub.domain;

// Lifecycle state of an auction item
public enum ItemStatus
{
    // the auction is running and accepting bids
    ACTIVE,
    // the auction ended with a winner, or was taken with buy-now
    SOLD,
    // the auction ended without a single bid
    CLOSED
}
