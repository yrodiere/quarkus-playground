package org.acme;

import io.smallrye.config.ConfigMapping;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Path("/hello")
public class GreetingResource {

    @ConfigMapping(prefix = "hello")
    public interface MyConfig {
        String message();
    }

    @Inject
    MyConfig config;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return config.message();
    }
}
