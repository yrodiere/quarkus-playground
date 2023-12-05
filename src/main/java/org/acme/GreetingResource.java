package org.acme;

import io.quarkus.logging.Log;
import io.smallrye.config.ConfigMapping;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
        Log.info("Info log for call to GreetingResource#hello");
        Log.trace("Trace log for call to GreetingResource#hello");
        return config.message();
    }
}
