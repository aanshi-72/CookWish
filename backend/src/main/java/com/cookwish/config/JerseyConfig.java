package com.cookwish.config;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        packages("com.cookwish");
        register(org.glassfish.jersey.jackson.JacksonFeature.class);
    }
}
