
package com.cookwish.security;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {

    @Override
    public void filter(
            ContainerRequestContext request,
            ContainerResponseContext response
    ) throws IOException {

        String origin = request.getHeaderString("Origin");
        String frontendOrigin = System.getenv("FRONTEND_ORIGIN");

        boolean allowed = "http://localhost:5173".equals(origin)
                || (frontendOrigin != null
                && frontendOrigin.equals(origin));

        if (allowed) {
            response.getHeaders().putSingle(
                    "Access-Control-Allow-Origin", origin
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
