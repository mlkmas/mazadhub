package com.mazadhub.api.mapper;

import com.mazadhub.api.dto.ApiError;
import com.mazadhub.exception.AlreadyHighestBidderException;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.exception.BuyNowNotAvailableException;
import com.mazadhub.exception.InvalidCredentialsException;
import com.mazadhub.exception.SellerCannotBidException;
import com.mazadhub.exception.ItemNotFoundException;
import com.mazadhub.exception.UserAlreadyExistsException;
import com.mazadhub.exception.UserNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.NoSuchElementException;

// Turns the application exceptions into HTTP responses with one uniform JSON error body
@Provider
public class RestExceptionMapper implements ExceptionMapper<RuntimeException>
{
    // Builds the JSON error body with the matching status code
    @Override
    public Response toResponse(RuntimeException ex)
    {
        Status s=classify(ex);
        ApiError body=new ApiError(s.error, ex.getMessage());
        return Response.status(s.code)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    // 404 not found, 401 bad credentials, 409 conflicts with the auction state, 400 bad input, 500 anything else
    private Status classify(RuntimeException ex)
    {
        if(ex instanceof ItemNotFoundException
                ||ex instanceof UserNotFoundException
                ||ex instanceof NoSuchElementException)
        {
            return new Status(404, "not_found");
        }

        if(ex instanceof InvalidCredentialsException)
        {
            return new Status(401, "unauthorized");
        }

        if(ex instanceof AlreadyHighestBidderException
                ||ex instanceof SellerCannotBidException
                ||ex instanceof BidTooLowException
                ||ex instanceof AuctionClosedException
                ||ex instanceof BuyNowNotAvailableException
                ||ex instanceof UserAlreadyExistsException)
        {
            return new Status(409, "conflict");
        }

        if(ex instanceof IllegalArgumentException)
        {
            return new Status(400, "bad_request");
        }

        return new Status(500, "internal_error");
    }

    // HTTP status plus the short error name put in the body
    private record Status(int code, String error)
    {
    }
}
