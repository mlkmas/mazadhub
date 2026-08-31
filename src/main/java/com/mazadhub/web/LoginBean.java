package com.mazadhub.web;

import com.mazadhub.domain.User;
import com.mazadhub.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

// Backs the login and registration screen
@Named
@RequestScoped
public class LoginBean
{
    // registration / login logic, injected by the container
    @Inject
    private UserService users;

    // where the signed-in user is stored
    @Inject
    private SessionBean session;

    // Login form
    private String username;
    private String password;

    // Register form
    private String regUsername;
    private String regPassword;
    private String regFullName;
    private String regEmail;
    private String regPhone;

    // Signs the user in and moves to the catalogue, or shows an error
    public String login()
    {
        try
        {
            User u=users.login(username, password);
            session.signIn(u);
            return "catalog?faces-redirect=true";
        }
        catch(RuntimeException e)
        {
            error("Login failed: invalid username or password.");
            return null;
        }
    }

    // Creates the account, signs the new user in and moves to the catalogue
    public String register()
    {
        try
        {
            User u=users.register(regUsername, regPassword, regFullName, regEmail, regPhone);
            session.signIn(u);
            return "catalog?faces-redirect=true";
        }
        catch(RuntimeException e)
        {
            error(e.getMessage()!=null?e.getMessage():"Registration failed.");
            return null;
        }
    }

    // Shows a red message on the page
    private void error(String msg)
    {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    // getters / setters used by the JSF pages and services
    public String getUsername()
    {
        return username;
    }

    public void setUsername(String v)
    {
        this.username=v;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String v)
    {
        this.password=v;
    }

    public String getRegUsername()
    {
        return regUsername;
    }

    public void setRegUsername(String v)
    {
        this.regUsername=v;
    }

    public String getRegPassword()
    {
        return regPassword;
    }

    public void setRegPassword(String v)
    {
        this.regPassword=v;
    }

    public String getRegFullName()
    {
        return regFullName;
    }

    public void setRegFullName(String v)
    {
        this.regFullName=v;
    }

    public String getRegEmail()
    {
        return regEmail;
    }

    public void setRegEmail(String v)
    {
        this.regEmail=v;
    }

    public String getRegPhone()
    {
        return regPhone;
    }

    public void setRegPhone(String v)
    {
        this.regPhone=v;
    }
}
