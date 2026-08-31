package com.mazadhub.support;

import java.lang.reflect.Field;

// Test helper that sets an entity id by reflection, standing in for what JPA does on persist
public final class TestIds
{
    // Utility class, never instantiated
    private TestIds()
    {
    }

    // Writes the id field and returns the same entity
    public static <T> T withId(T entity, long id)
    {
        try
        {
            Field f=entity.getClass().getDeclaredField("id");
            f.setAccessible(true);
            f.set(entity, id);
            return entity;
        }
        catch(ReflectiveOperationException e)
        {
            throw new IllegalStateException("Could not set id on "+entity.getClass(), e);
        }
    }
}
