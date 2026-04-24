package org.acme.storedprocedure;

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
 * Common interface for blocking stored procedure endpoints
 * (Hibernate ORM, JDBC).
 */
public interface StoredProcedureEndpoints {

    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_PLAIN)
    String profile();

    @POST
    @Path("/procedure/without-params")
    @Produces(MediaType.TEXT_PLAIN)
    String callProcedureWithoutParams() throws Exception;

    @POST
    @Path("/procedure/with-input-params")
    @Produces(MediaType.TEXT_PLAIN)
    String callProcedureWithInputParams(@RestQuery String username) throws Exception;

    // Functions (with return values)

    @GET
    @Path("/function/return-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Integer callFunctionReturningBasicType() throws Exception;

    @GET
    @Path("/function/return-tuples")
    @Produces(MediaType.APPLICATION_JSON)
    List<ReturnedUser> callFunctionReturningTuples() throws Exception;

    @GET
    @Path("/function/return-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserProfile> callFunctionReturningEntitiesNoAssociation() throws Exception;

    @GET
    @Path("/function/return-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserActivity> callFunctionReturningEntitiesWithToOne() throws Exception;

    // Procedures (with output parameters)

    @GET
    @Path("/procedure/output-param-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Integer callProcedureWithOutputParamBasicType() throws Exception;

    @GET
    @Path("/procedure/output-param-tuples")
    @Produces(MediaType.APPLICATION_JSON)
    List<ReturnedUser> callProcedureWithOutputParamTuples() throws Exception;

    @GET
    @Path("/procedure/output-param-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserProfile> callProcedureWithOutputParamEntitiesNoAssociation() throws Exception;

    @GET
    @Path("/procedure/output-param-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserActivity> callProcedureWithOutputParamEntitiesWithToOne() throws Exception;
}
