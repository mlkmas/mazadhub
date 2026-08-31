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

// Registration and login
@ApplicationScoped
public class UserService
{
    private UserRepository users;

    // CDI needs a no-argument constructor
    protected UserService()
    {
    }

    // The user repository is injected by the container
    @Inject
    public UserService(UserRepository users)
    {
        this.users=users;
    }

    // Creates an account with a hashed password, refusing a name that is taken
    @Transactional
    public User register(String username, String rawPassword,
                         String fullName, String email, String phone)
    {
        if(users.existsByUsername(username))
        {
            throw new UserAlreadyExistsException(username);
        }

        User user=new User(username, PasswordHasher.hash(rawPassword), UserRole.USER);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPhone(phone);
        return users.save(user);
    }

    // Checks the password and returns the user, or throws InvalidCredentialsException
    public User login(String username, String rawPassword)
    {
        User user=users.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);
        if(!PasswordHasher.verify(rawPassword, user.getPasswordHash()))
        {
            throw new InvalidCredentialsException();
        }

        return user;
    }
}
