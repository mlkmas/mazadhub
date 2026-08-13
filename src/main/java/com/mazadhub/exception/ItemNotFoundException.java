package com.mazadhub.exception;

/** Thrown when an operation references an item id that does not exist. */
public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(long itemId) {
        super("Item not found: " + itemId);
    }
}
