package com.mazadhub.api.dto;

import com.mazadhub.domain.Category;

// Read model for a category
public record CategoryDTO(Long id, String name, String description)
{
    // Copies a category into the DTO
    public static CategoryDTO from(Category c)
    {
        return new CategoryDTO(c.getId(), c.getName(), c.getDescription());
    }
}
