package com.mazadhub.api.mapper;

import com.mazadhub.api.dto.ApiError;
import com.mazadhub.exception.AuctionClosedException;
import com.mazadhub.exception.BidTooLowException;
import com.mazadhub.exception.BuyNowNotAvailableException;
import com.mazadhub.exception.InvalidCredentialsException;
import com.mazadhub.exception.ItemNotFoundException;
import com.mazadhub.exception.UserAlreadyExistsException;
import com.mazadhub.exception.UserNotFoundException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.NoSuchElementException;

/**
 * Turns the application's exceptions into clean HTTP responses with a uniform
 * JSON error body, so every endpoint reports failures the same way:
 *
 * <ul>
 *   <li>404 — item / user / category not found</li>
 *   <li>401 — invalid credentials</li>
 *   <li>409 — conflict with current state (bid too low, auction closed,
 *       buy-now unavailable, username taken)</li>
 *   <li>400 — bad input (negative price, non-positive duration, …)</li>
 *   <li>500 — anything unexpected</li>
 * </ul>
 */
@Provider
public class RestExceptionMapper implements ExceptionMapper<RuntimeException> {

    @Override
    public Response toResponse(RuntimeException ex) {
        Status s = classify(ex);
        ApiError body = new ApiError(s.error, ex.getMessage());
        return Response.status(s.code)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }

    private Status classify(RuntimeException ex) {
        if (ex instanceof ItemNotFoundException
                || ex instanceof UserNotFoundException
                || ex instanceof NoSuchElementException) {
            return new Status(404, "not_found");
        }
        if (ex instanceof InvalidCredentialsException) {
            return new Status(401, "unauthorized");
        }
        if (ex instanceof BidTooLowException
                || ex instanceof AuctionClosedException
                || ex instanceof BuyNowNotAvailableException
                || ex instanceof UserAlreadyExistsException) {
            return new Status(409, "conflict");
        }
        if (ex instanceof IllegalArgumentException) {
            return new Status(400, "bad_request");
        }
        return new Status(500, "internal_error");
    }

    private record Status(int code, String error) {
    }
}
