package com.mazadhub.web;

import com.mazadhub.domain.User;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;

// Keeps the signed-in user for as long as the browser session lasts
@Named
@SessionScoped
public class SessionBean implements Serializable
{
    private Long userId;
    private String username;
    private String fullName;
    private String role; // "USER" or "ADMIN"

    // Called by LoginBean once a login or registration succeeded
    public void signIn(User user)
    {
        this.userId=user.getId();
        this.username=user.getUsername();
        this.fullName=user.getFullName();
        this.role=user.getRole().name();
    }

    // Clears the session and sends the visitor back to the catalogue
    public String logout()
    {
        userId=null;
        username=null;
        fullName=null;
        role=null;
        return "catalog?faces-redirect=true";
    }

    // True when somebody is signed in
    public boolean isLoggedIn()
    {
        return userId!=null;
    }

    // True when the signed-in user is an administrator
    public boolean isAdmin()
    {
        return "ADMIN".equals(role);
    }

    // Full name if we have one, otherwise the username
    public String getDisplayName()
    {
        return fullName!=null&&!fullName.isBlank()?fullName:username;
    }

    // getters / setters used by the JSF pages and services
    public Long getUserId()
    {
        return userId;
    }

    public String getUsername()
    {
        return username;
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getRole()
    {
        return role;
    }
}
