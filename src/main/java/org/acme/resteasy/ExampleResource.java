package org.acme.resteasy;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/resteasy/hello")
public class ExampleResource {

	@Inject
	EntityManager em;
	
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String merge() {
    	Parent parent = new Parent();
    	parent.setId(1L);
    	parent.setName("new name");
    	
    	em.merge(parent);
    	
    	
        return "hello";
    }
}