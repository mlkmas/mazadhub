package com.mazadhub.api;

import com.mazadhub.api.dto.LoginRequest;
import com.mazadhub.api.dto.RegisterRequest;
import com.mazadhub.api.dto.UserDTO;
import com.mazadhub.domain.User;
import com.mazadhub.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

// REST endpoints for registration and login
@Path("users")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource
{
    private UserService users;

    // CDI needs a no-argument constructor
    protected UserResource()
    {
    }

    // The user service is injected by the container
    @Inject
    public UserResource(UserService users)
    {
        this.users=users;
    }

    // POST /api/users/register - creates an account, answers 201
    @POST
    @Path("register")
    public Response register(RegisterRequest req)
    {
        User u=users.register(req.username(), req.password(),
                req.fullName(), req.email(), req.phone());
        return Response.status(Response.Status.CREATED).entity(UserDTO.from(u)).build();
    }

    // POST /api/users/login - checks credentials and returns the user
    @POST
    @Path("login")
    public UserDTO login(LoginRequest req)
    {
        return UserDTO.from(users.login(req.username(), req.password()));
    }
}
