package org.acme;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/myentity")
public class MyEntityResource {

    @Inject
    EntityManager entityManager;

    @GET
    @Path("count")
    @Produces(MediaType.TEXT_PLAIN)
    public String count() {
        return doCount().toString();
    }

    @Transactional
    public Long doCount() {
        return (Long) entityManager.createQuery("select count(*) from MyEntity").getSingleResult();
    }

    @POST
    @Path("create")
    @Produces(MediaType.TEXT_PLAIN)
    public String create() {
        // This is a write operation and thus will only work if the @Transactional annotation is effective
        return doCreate().toString();
    }

    @Transactional
    public Long doCreate() {
        var entity = new MyEntity();
        entity.setField("foo");
        entityManager.persist(entity);
        return entity.getId();
    }
}