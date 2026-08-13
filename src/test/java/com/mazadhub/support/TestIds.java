package com.mazadhub.support;

import java.lang.reflect.Field;

/**
 * Test-only helper that assigns the generated {@code id} of an entity via
 * reflection, simulating what the JPA provider does on persist. Keeps the
 * production entities free of public id setters.
 */
public final class TestIds {

    private TestIds() {
    }

    public static <T> T withId(T entity, long id) {
        try {
            Field f = entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not set id on " + entity.getClass(), e);
        }
    }
}
