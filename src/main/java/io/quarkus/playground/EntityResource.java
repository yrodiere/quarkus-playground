package io.quarkus.playground;

import org.hibernate.Hibernate;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/entity")
public class EntityResource {

    @PersistenceContext
    EntityManager entityManager;

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
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
    @Path("/{id}/is-contained-initialized/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public boolean isContainedInitialized(@PathParam long id) {
        Containing containing = entityManager.find(Containing.class, id);
        return Hibernate.isInitialized(containing.getContained());
    }
}