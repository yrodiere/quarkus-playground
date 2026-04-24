package org.acme.storedprocedure;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

/**
 * Common interface for reactive stored procedure endpoints
 * (Hibernate Reactive, Vert.x Reactive SQL Client).
 */
public interface ReactiveStoredProcedureEndpoints {

    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_PLAIN)
    String profile();

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callNoParams();

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callWithInputParams(@RestQuery String username);

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Integer> callWithOutputParams();

    @GET
    @Path("/return-data-result-set")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<ReturnedUser>> callReturningDataAsResultSet();

    @GET
    @Path("/return-data-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Integer> callReturningDataAsBasicType();

    @GET
    @Path("/return-data-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserProfile>> callReturningDataAsEntitiesNoAssociation();

    @GET
    @Path("/return-data-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserActivity>> callReturningDataAsEntitiesWithToOne();

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<ReturnedUser>> callWithCursor();
}
