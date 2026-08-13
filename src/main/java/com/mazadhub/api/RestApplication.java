package com.mazadhub.api;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Activates JAX-RS and roots the whole REST API under {@code /api}.
 * With this annotation present no web.xml servlet mapping is needed; the
 * container discovers the resource and provider classes automatically.
 *
 * <p>Full base path once deployed: {@code /<context-root>/api/...}
 */
@ApplicationPath("api")
public class RestApplication extends Application {
    // Empty on purpose: classpath scanning discovers @Path and @Provider classes.
}
