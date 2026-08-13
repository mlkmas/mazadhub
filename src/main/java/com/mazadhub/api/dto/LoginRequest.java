package com.mazadhub.api.dto;

/** Request body for POST /api/users/login. */
public record LoginRequest(String username, String password) {
}
