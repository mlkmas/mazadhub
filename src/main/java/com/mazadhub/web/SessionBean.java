package com.mazadhub.web;

import com.mazadhub.domain.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

/**
 * Holds the signed-in user for the length of the browser session. Stores plain
 * fields (not the JPA entity) to avoid detached-entity issues across requests.
 */
@Named
@SessionScoped
public class SessionBean implements Serializable {

    private Long userId;
    private String username;
    private String fullName;
    private String role;   // "USER" or "ADMIN"

    /** Called by LoginBean after a successful login/registration. */
    public void signIn(User user) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.fullName = user.getFullName();
        this.role = user.getRole().name();
    }

    public String logout() {
        userId = null;
        username = null;
        fullName = null;
        role = null;
        return "catalog?faces-redirect=true";
    }

    public boolean isLoggedIn() {
        return userId != null;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public String getDisplayName() {
        return fullName != null && !fullName.isBlank() ? fullName : username;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
