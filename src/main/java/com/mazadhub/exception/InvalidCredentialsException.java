package com.mazadhub.exception;

// Thrown when login fails due to an unknown user or wrong password
public class InvalidCredentialsException extends RuntimeException
{
    public InvalidCredentialsException()
    {
        super("Invalid username or password");
    }
}
