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
    @Path("/procedure/without-params")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callProcedureWithoutParams();

    @POST
    @Path("/procedure/with-input-params")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<String> callProcedureWithInputParams(@RestQuery String username);

    // Functions (with return values)

    @GET
    @Path("/function/return-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Integer> callFunctionReturningBasicType();

    @GET
    @Path("/function/return-tuples")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<ReturnedUser>> callFunctionReturningTuples();

    @GET
    @Path("/function/return-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserProfile>> callFunctionReturningEntitiesNoAssociation();

    @GET
    @Path("/function/return-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserActivity>> callFunctionReturningEntitiesWithToOne();

    // Procedures (with output parameters)

    @GET
    @Path("/procedure/output-param-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Uni<Integer> callProcedureWithOutputParamBasicType();

    @GET
    @Path("/procedure/output-param-tuples")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<ReturnedUser>> callProcedureWithOutputParamTuples();

    @GET
    @Path("/procedure/output-param-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserProfile>> callProcedureWithOutputParamEntitiesNoAssociation();

    @GET
    @Path("/procedure/output-param-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    Uni<List<UserActivity>> callProcedureWithOutputParamEntitiesWithToOne();
}
