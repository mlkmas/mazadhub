package com.mazadhub.domain;

/**
 * ACTIVE–auction is running and accepting bids.
 * SOLD– auction ended with a winning bid.
 * CLOSED–auction ended with no winner(no bids,or cancelled).
 */
public enum ItemStatus
{
    ACTIVE,
    SOLD,
    CLOSED
}
