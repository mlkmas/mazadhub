package com.mazadhub.api.dto;

import com.mazadhub.domain.User;

// Read model for a user returned after register/login
public record UserDTO(Long id, String username, String fullName, String role)
{
    // Copies a user into the DTO, leaving the password hash behind
    public static UserDTO from(User u)
    {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name());
    }
}
