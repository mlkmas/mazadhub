package com.mazadhub.domain;

import java.io.Serializable;

/**
 * Implemented by all persistent entities, exposing their generated identifier.
 * Lets {@link com.mazadhub.repository.AbstractRepository} decide generically
 * whether an entity is new (persist) or detached (merge).
 *
 * <p>Extends {@link Serializable} so that every entity is serializable. This is
 * both recommended JPA practice and required for entities to be held by a
 * CDI {@code @ViewScoped} bean (e.g. the item page), whose state JSF must be
 * able to save and restore across form posts.
 */
public interface Identifiable extends Serializable {

    Long getId();
}
