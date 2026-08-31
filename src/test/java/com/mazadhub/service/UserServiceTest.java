package com.mazadhub.service;

import com.mazadhub.domain.User;
import com.mazadhub.exception.InvalidCredentialsException;
import com.mazadhub.exception.UserAlreadyExistsException;
import com.mazadhub.support.Fakes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Unit tests for registration and login against an in-memory user store
class UserServiceTest
{
    private Fakes.Users users;
    private UserService service;

    // A fresh user store and service before every test
    @BeforeEach
    void setUp()
    {
        users=new Fakes.Users();
        service=new UserService(users);
    }

    // The saved user keeps a hash, never the typed password
    @Test
    void registerStoresHashedPassword()
    {
        User u=service.register("alice", "p@ss", "Alice", "a@x.com", "050");
        assertEquals("alice", u.getUsername());
        assertNotEquals("p@ss", u.getPasswordHash());
    }

    // A second account with the same name is refused
    @Test
    void registerRejectsDuplicateUsername()
    {
        service.register("alice", "p@ss", "Alice", "a@x.com", "050");
        assertThrows(UserAlreadyExistsException.class,
                ()->service.register("alice", "other", "Alice2", "a2@x.com", "051"));
    }

    // The right password returns the user
    @Test
    void loginSucceedsWithCorrectPassword()
    {
        service.register("bob", "secret", "Bob", "b@x.com", "052");
        User u=service.login("bob", "secret");
        assertEquals("bob", u.getUsername());
    }

    // A wrong password is refused
    @Test
    void loginFailsWithWrongPassword()
    {
        service.register("bob", "secret", "Bob", "b@x.com", "052");
        assertThrows(InvalidCredentialsException.class, ()->service.login("bob", "nope"));
    }

    // An unknown name gives the same error, so nothing is leaked
    @Test
    void loginFailsForUnknownUser()
    {
        assertThrows(InvalidCredentialsException.class, ()->service.login("ghost", "x"));
    }
}
