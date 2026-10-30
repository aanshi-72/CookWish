package com.cookwish.security;

import com.cookwish.util.JwtUtil;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    private static final Set<String> PUBLIC_PATHS = new HashSet<>(Arrays.asList("auth/login"));
    @Override public void filter(ContainerRequestContext request) throws IOException {
        String path = request.getUriInfo().getPath();
        if (PUBLIC_PATHS.contains(path)) return;
        String method = request.getMethod();
        if (method.equals("OPTIONS")) return;
        String header = request.getHeaderString("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\":\"Missing bearer token\"}").type("application/json").build());
            return;
        }
        JwtUtil.Claims claims = JwtUtil.validate(header.substring(7).trim());
        if (claims == null) {
            request.abortWith(Response.status(Response.Status.UNAUTHORIZED).entity("{\"message\":\"Invalid or expired token\"}").type("application/json").build());
            return;
        }
        request.setProperty("cookwish.username", claims.username);
        request.setProperty("cookwish.role", claims.role);
        if (isWrite(method) && !"ADMIN".equals(claims.role)) {
            request.abortWith(Response.status(Response.Status.FORBIDDEN).entity("{\"message\":\"Admin role required\"}").type("application/json").build());
        }
    }
    private boolean isWrite(String method) {
        return method.equals("POST") || method.equals("PUT") || method.equals("PATCH") || method.equals("DELETE");
    }
}
