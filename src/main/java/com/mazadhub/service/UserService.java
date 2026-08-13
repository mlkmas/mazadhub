package com.mazadhub.service;

import com.mazadhub.domain.User;
import com.mazadhub.domain.UserRole;
import com.mazadhub.exception.InvalidCredentialsException;
import com.mazadhub.exception.UserAlreadyExistsException;
import com.mazadhub.repository.UserRepository;
import com.mazadhub.security.PasswordHasher;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

/**
 * Registration and authentication of users. Passwords are hashed before storage
 * and never persisted in clear text.
 */
@ApplicationScoped
public class UserService {

    private UserRepository users;

    protected UserService() {
        // for the CDI proxy
    }

    @Inject
    public UserService(UserRepository users) {
        this.users = users;
    }

    /**
     * Registers a new end user.
     *
     * @throws UserAlreadyExistsException if the username is taken
     */
    @Transactional
    public User register(String username, String rawPassword,
                         String fullName, String email, String phone) {
        if (users.existsByUsername(username)) {
            throw new UserAlreadyExistsException(username);
        }
        User user = new User(username, PasswordHasher.hash(rawPassword), UserRole.USER);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        return users.save(user);
    }

    /**
     * Authenticates a user by username and password.
     *
     * @return the authenticated user
     * @throws InvalidCredentialsException if the user is unknown or the password is wrong
     */
    public User login(String username, String rawPassword) {
        User user = users.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if (!PasswordHasher.verify(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return user;
    }
}
