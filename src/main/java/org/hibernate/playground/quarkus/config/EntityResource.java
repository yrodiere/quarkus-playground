package org.hibernate.playground.quarkus.config;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/entity")
public class EntityResource {

    @Inject
    EntityManager em;

    @GET
    @Path("test")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public Integer test() {
        MyEntity entity = new MyEntity();
        em.persist(entity);
        return entity.getId();
    }
}
