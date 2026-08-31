package com.mazadhub.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

// Switches JAX-RS on and puts the whole REST API under /api
@ApplicationPath("api")
public class RestApplication extends Application
{
    // Empty on purpose: classpath scanning discovers @Path and @Provider classes.
}
