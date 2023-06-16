package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/entity")
public class MyEntityResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get(UUID id) {
        return MyEntity.findById(id).toString();
    }
}
