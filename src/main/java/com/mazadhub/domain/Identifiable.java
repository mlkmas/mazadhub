package com.mazadhub.domain;

/**
 * Implemented by all persistent entities, exposing their generated identifier.
 * Lets {@link com.mazadhub.repository.AbstractRepository} decide generically
 * whether an entity is new (persist) or detached (merge).
 */
public interface Identifiable
{

    Long getId();
}
