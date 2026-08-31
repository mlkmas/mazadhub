package com.mazadhub.domain;

import java.io.Serializable;

// Implemented by every entity, so generic code can read the primary key
public interface Identifiable extends Serializable
{
    // The generated primary key, or null before the entity is saved
    Long getId();
}
