package com.mazadhub.repository;

import com.mazadhub.domain.Identifiable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

// Base class giving every repository the same find / save / delete methods
public abstract class AbstractRepository<T extends Identifiable>
{
    // the JPA entity manager, injected by the container
    @PersistenceContext(unitName="mazadhubPU")
    protected EntityManager em;

    private final Class<T> entityClass;

    // Subclasses pass the entity type they manage
    protected AbstractRepository(Class<T> entityClass)
    {
        this.entityClass=entityClass;
    }

    // Looks one row up by primary key
    public Optional<T> findById(Long id)
    {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    // Inserts when the entity has no id yet, otherwise updates it
    public T save(T entity)
    {
        if(entity.getId()==null)
        {
            em.persist(entity);
            return entity;
        }

        return em.merge(entity);
    }

    // Removes the row, re-attaching the entity first if needed
    public void delete(T entity)
    {
        em.remove(em.contains(entity)?entity:em.merge(entity));
    }
}
