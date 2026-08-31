package com.mazadhub.api.dto;

// Request body for POST /api/users/register
public record RegisterRequest(String username, String password,
                              String fullName, String email, String phone)
{
}
