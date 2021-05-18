package io.quarkus.playground;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello-resteasy")
public class GreetingResource {

    @PersistenceContext
    EntityManager entityManager;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public void create(@PathParam long id) {
        Containing containing = new Containing();
        containing.setId(id);
        entityManager.persist(containing);

        for (int i = 0; i < 10; i++) {
            Contained contained = new Contained();
            contained.setContaining(containing);
            containing.getContained().add(contained);
            entityManager.persist(contained);
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public boolean isInitialized(@PathParam long id) {
        Containing containing = new Containing();
        entityManager.persist(containing);

    }
}