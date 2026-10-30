
package com.cookwish.security;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.Priorities;
import javax.annotation.Priority;
import java.io.IOException;

@Provider
@Priority(Priorities.AUTHENTICATION - 1)
public class CorsFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext request) throws IOException {

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            request.abortWith(
                javax.ws.rs.core.Response.ok().build()
            );
        }
    }

    @Override
    public void filter(
            ContainerRequestContext request,
            ContainerResponseContext response
    ) throws IOException {

        String origin = request.getHeaderString("Origin");
        String frontendOrigin = System.getenv("FRONTEND_ORIGIN");

        boolean allowed = "http://localhost:5173".equals(origin)
                || (frontendOrigin != null && frontendOrigin.equals(origin));

        if (allowed) {
            response.getHeaders().putSingle(
                "Access-Control-Allow-Origin", origin
            );
            response.getHeaders().putSingle(
                "Access-Control-Allow-Credentials", "true"
            );
        }

        response.getHeaders().putSingle("Vary", "Origin");
        response.getHeaders().putSingle(
            "Access-Control-Allow-Headers",
            "origin, content-type, accept, authorization"
        );
        response.getHeaders().putSingle(
            "Access-Control-Allow-Methods",
            "GET, POST, PUT, DELETE, OPTIONS, HEAD"
        );
        response.getHeaders().putSingle(
            "Access-Control-Max-Age", "3600"
        );
    }
}
