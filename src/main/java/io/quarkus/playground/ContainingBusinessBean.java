package io.quarkus.playground;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

@RequestScoped
public class ContainingBusinessBean {

    @Inject
    EntityManager em;

    public Role findRoleById(Long id) throws Exception {
        if (id != null) {
            return em.find(Role.class, id);
        }
        return null;
    }

    public Module findModuleById(Long id) throws Exception {
        if (id != null) {
            return em.find(Module.class, id);
        }
        return null;
    }

}
