
package com.cookwish.resource;

import com.cookwish.model.LoginRequest;
import com.cookwish.util.JwtUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    @POST
    @Path("/login")
    public Response login(LoginRequest request) {

        if (request == null
                || request.getUsername() == null
                || request.getPassword() == null) {
            return Response.status(400)
                    .entity(message("Username and password are required"))
                    .build();
        }

        String username = request.getUsername().trim();
        String password = request.getPassword();

        String adminUsername = System.getenv("ADMIN_USERNAME");
        String adminPassword = System.getenv("ADMIN_PASSWORD");
        String userUsername = System.getenv("USER_USERNAME");
        String userPassword = System.getenv("USER_PASSWORD");

        String role = null;

        if (adminUsername != null && adminPassword != null
                && adminUsername.equals(username)
                && adminPassword.equals(password)) {
            role = "ADMIN";
        } else if (userUsername != null && userPassword != null
                && userUsername.equals(username)
                && userPassword.equals(password)) {
            role = "USER";
        }

        if (role == null) {
            return Response.status(401)
                    .entity(message("Invalid username or password"))
                    .build();
        }

        try {
            Map<String, Object> result = new LinkedHashMap<>();

            result.put("token", JwtUtil.createToken(username, role));
            result.put("username", username);
            result.put("role", role);

            return Response.ok(result).build();

        } catch (Exception e) {
            return Response.serverError()
                    .entity(message("Could not create login token"))
                    .build();
        }
    }

    private Map<String, String> message(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("message", text);
        return result;
    }
}
