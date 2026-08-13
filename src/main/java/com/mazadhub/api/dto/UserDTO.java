package com.mazadhub.api.dto;

import com.mazadhub.domain.User;

/**
 * Read model for a user returned after register/login. Never carries the
 * password hash.
 */
public record UserDTO(Long id, String username, String fullName, String role) {

    public static UserDTO from(User u) {
        return new UserDTO(u.getId(), u.getUsername(), u.getFullName(), u.getRole().name());
    }
}
