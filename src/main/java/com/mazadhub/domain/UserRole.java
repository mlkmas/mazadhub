package com.mazadhub.domain;

// What a signed-in account is allowed to do
public enum UserRole
{
    // browses, sells items and places bids
    USER,
    // also manages categories and other users
    ADMIN
}
