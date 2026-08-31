package com.mazadhub.exception;

// Thrown when an operation references a user id that does not exist
public class UserNotFoundException extends RuntimeException
{
    public UserNotFoundException(long userId)
    {
        super("User not found: "+userId);
    }
}
