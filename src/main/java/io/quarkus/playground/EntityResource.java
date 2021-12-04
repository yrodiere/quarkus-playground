package io.quarkus.playground;

import java.util.ArrayList;
import java.util.logging.Logger;
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
import org.jboss.logmanager.Level;

@Path("/entity")
public class EntityResource {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public void create(@PathParam long id) {
        Containing containing = new Containing();
        containing.setId(id);

        Containing2 containing2 = new Containing2();
        containing2.setId(id);
        entityManager.persist(containing2);

        for (int i = 0; i < 5; i++) {
            Contained contained = new Contained();
            contained.setContaining(containing);
            contained.setContaining2(containing2);
            containing.getContained().add(contained);
        }

        entityManager.persist(containing);
    }

    @GET
    @Path("/{id}/is-contained-initialized/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public boolean isContainedInitialized(@PathParam long id) {
        Containing containing = entityManager.find(Containing.class, id);
        return Hibernate.isInitialized(containing.getContained());
    }

    @GET
    @Path("/{id}/is-contained-initialized-after-merge/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public boolean isContainedInitializedAfterMerge(@PathParam long id) {
        Containing containing = entityManager.find(Containing.class, id);
        Containing2 containing2 = entityManager.find(Containing2.class, id);
        entityManager.detach(containing);
        entityManager.detach(containing2);

        containing.setContained(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            Contained contained = new Contained();
            contained.setContaining(containing);
            contained.setContaining2(containing2);
            containing.getContained().add(contained);
        }
        containing = entityManager.merge(containing);

        LOGGER.log(Level.SEVERE, "INIT:" + Hibernate.isInitialized(containing.getContained()));
        LOGGER.log(Level.SEVERE, "INIT:" + Hibernate.isInitialized(containing.getContained().get(0)));
        LOGGER.log(Level.SEVERE, "INIT:" + Hibernate.isInitialized(containing.getContained().get(0).getContaining2()));
        LOGGER.log(Level.SEVERE, "INIT:" + Hibernate.isInitialized(containing.getContained().get(0).getContaining2().getContained()));

        return Hibernate.isInitialized(containing.getContained().get(0).getContaining2().getContained()); //Must be false
    }

    @GET
    @Path("/{id}/lazy-exception/")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public Containing lazyException(@PathParam long id) {

        try {
            Containing containing = entityManager.find(Containing.class, id);
            Containing2 containing2 = entityManager.find(Containing2.class, id);
            entityManager.detach(containing);
            entityManager.detach(containing2);

            containing.setContained(new ArrayList<>());
            for (int i = 0; i < 5; i++) {
                Contained contained = new Contained();
                contained.setContaining(containing);
                contained.setContaining2(containing2);
                containing.getContained().add(contained);
            }
            containing = entityManager.merge(containing);

            return containing; //Hibernate5Module and CustomLazySerializer not working? 

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            throw ex;
        }
    }
}
