package com.mazadhub.exception;

// Thrown when registering a username that is already taken
public class UserAlreadyExistsException extends RuntimeException
{
    public UserAlreadyExistsException(String username)
    {
        super("Username already exists: "+username);
    }
}
