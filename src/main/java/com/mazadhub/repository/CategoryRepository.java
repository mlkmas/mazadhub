package com.mazadhub.repository;

import com.mazadhub.domain.Category;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

/**
 * Data-access operations for {@link Category}.
 */
@ApplicationScoped
public class CategoryRepository extends AbstractRepository<Category> {

    public CategoryRepository() {
        super(Category.class);
    }

    public List<Category> findAll() {
        return em.createQuery("SELECT c FROM Category c ORDER BY c.name", Category.class)
                .getResultList();
    }
}
