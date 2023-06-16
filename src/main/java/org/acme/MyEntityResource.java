package org.acme;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.UUID;

@Path("/entity")
public class MyEntityResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String get(UUID id) {
        return entityManager.find(MyEntity.class, id).toString();
    }
}
