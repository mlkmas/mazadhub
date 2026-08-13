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

/** Read-only catalogue of categories. */
@Path("categories")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class CategoryResource {

    private ItemService items;

    protected CategoryResource() {
        // for the CDI proxy
    }

    @Inject
    public CategoryResource(ItemService items) {
        this.items = items;
    }

    @GET
    @Transactional
    public List<CategoryDTO> list() {
        return items.listCategories().stream().map(CategoryDTO::from).toList();
    }
}
