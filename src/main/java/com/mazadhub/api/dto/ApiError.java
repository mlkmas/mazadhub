package com.mazadhub.api.dto;

/** Uniform JSON error body returned by the exception mapper. */
public record ApiError(String error, String message) {
}
