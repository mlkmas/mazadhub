package com.mazadhub.repository;

import com.mazadhub.domain.Category;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

// Data-access operations for Category
@ApplicationScoped
public class CategoryRepository extends AbstractRepository<Category>
{
    // Tells the base class which entity this repository manages
    public CategoryRepository()
    {
        super(Category.class);
    }

    // Every category, in alphabetical order
    public List<Category> findAll()
    {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.name", Category.class)
                .getResultList();
    }
}
