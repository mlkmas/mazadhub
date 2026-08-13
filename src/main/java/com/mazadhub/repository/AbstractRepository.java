package com.mazadhub.repository;

import com.mazadhub.domain.Identifiable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

/**
 * Generic JPA repository base. Concrete repositories extend it with the entity
 * type, inheriting the common find/save/delete operations. The shared
 * {@link EntityManager} is container-injected.
 *
 * @param <T> entity type, which must expose its id via {@link Identifiable}
 */
public abstract class AbstractRepository<T extends Identifiable> {

    @PersistenceContext(unitName = "bidhubPU")
    protected EntityManager em;

    private final Class<T> entityClass;

    protected AbstractRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public Optional<T> findById(Long id) {
        return Optional.ofNullable(em.find(entityClass, id));
    }

    /**
     * Persists a new entity or merges a detached one, deciding by whether the
     * id has been assigned yet.
     *
     * @return the managed instance
     */
    public T save(T entity) {
        if (entity.getId() == null) {
            em.persist(entity);
            return entity;
        }
        return em.merge(entity);
    }

    public void delete(T entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}
