package io.quarkus.playground;

import jakarta.inject.Inject;
import java.util.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/entity")
public class EntityResource {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

    @PersistenceContext
    EntityManager entityManager;

    @Inject
    ContainingBusinessBean bean;

    @PUT
    @Path("/{id}/")
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public void create(@PathParam long id) {

        System system = new System();
        entityManager.persist(system);

        Module module = new Module();
        module.setId(id);
        module.setSystem(system);
        entityManager.persist(module);

        Role role = new Role();
        role.setId(id);
        role.setModule(module);
        role.setSystem(system);
        entityManager.persist(role);
        
        Operation op = new Operation();
        op.setCodigo("OP1");
        op.setModulo(module);
        op.setSistema(system);
        entityManager.persist(op);
        
        Operation op2 = new Operation();
        op2.setCodigo("OP2");
        op2.setModulo(module);
        op2.setSistema(system);
        entityManager.persist(op2);
        
        RolOperation rolOpe = new RolOperation();
        rolOpe.setId(1L);
        rolOpe.setOperacion(op);
        rolOpe.setRol(role);
        entityManager.persist(rolOpe);
        
        RolOperation rolOpe2 = new RolOperation();
        rolOpe2.setId(2L);
        rolOpe2.setOperacion(op2);
        rolOpe2.setRol(role);
        entityManager.persist(rolOpe2);

    }

    @PUT
    @Path("/test/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public void update(@PathParam long id) throws Exception {
        Role role = bean.findRoleById(id);
        save(role);
    }

    @Transactional
    public void save(Role role) throws Exception {
        Module cont2 = bean.findModuleById(role.getModule().getId());
        entityManager.merge(role);
    }

}
