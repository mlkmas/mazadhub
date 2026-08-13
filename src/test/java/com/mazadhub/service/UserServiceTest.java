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

class UserServiceTest {

    private Fakes.Users users;
    private UserService service;

    @BeforeEach
    void setUp() {
        users = new Fakes.Users();
        service = new UserService(users);
    }

    @Test
    void registerStoresHashedPassword() {
        User u = service.register("alice", "p@ss", "Alice", "a@x.com", "050");
        assertEquals("alice", u.getUsername());
        assertNotEquals("p@ss", u.getPasswordHash());
    }

    @Test
    void registerRejectsDuplicateUsername() {
        service.register("alice", "p@ss", "Alice", "a@x.com", "050");
        assertThrows(UserAlreadyExistsException.class,
                () -> service.register("alice", "other", "Alice2", "a2@x.com", "051"));
    }

    @Test
    void loginSucceedsWithCorrectPassword() {
        service.register("bob", "secret", "Bob", "b@x.com", "052");
        User u = service.login("bob", "secret");
        assertEquals("bob", u.getUsername());
    }

    @Test
    void loginFailsWithWrongPassword() {
        service.register("bob", "secret", "Bob", "b@x.com", "052");
        assertThrows(InvalidCredentialsException.class, () -> service.login("bob", "nope"));
    }

    @Test
    void loginFailsForUnknownUser() {
        assertThrows(InvalidCredentialsException.class, () -> service.login("ghost", "x"));
    }
}
