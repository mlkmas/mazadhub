package com.mazadhub.api;

import com.mazadhub.api.dto.CategoryDTO;
import com.mazadhub.service.ItemService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

// REST endpoint listing the catalogue categories
@Path("categories")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource
{
    private ItemService items;

    // CDI needs a no-argument constructor
    protected CategoryResource()
    {
    }

    // The item service is injected by the container
    @Inject
    public CategoryResource(ItemService items)
    {
        this.items=items;
    }

    // GET /api/categories - every category
    @GET
    @Transactional
    public List<CategoryDTO> list()
    {
        return items.listCategories().stream().map(CategoryDTO::from).toList();
    }
}
