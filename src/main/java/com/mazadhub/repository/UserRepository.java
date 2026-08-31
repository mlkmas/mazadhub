package com.mazadhub.repository;

import com.mazadhub.domain.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.Optional;

// Data-access operations for User
@ApplicationScoped
public class UserRepository extends AbstractRepository<User>
{
    // Tells the base class which entity this repository manages
    public UserRepository()
    {
        super(User.class);
    }

    // Looks a user up by login name, empty if there is none
    public Optional<User> findByUsername(String username)
    {
        try
        {
            return Optional.of(em.createQuery(
                            "SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult());
        }
        catch(NoResultException e)
        {
            return Optional.empty();
        }
    }

    // True if the username is already taken
    public boolean existsByUsername(String username)
    {
        Long count=em.createQuery(
                        "SELECT COUNT(u) FROM User u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count>0;
    }
}
