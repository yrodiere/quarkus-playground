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
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    String callNoParams() throws Exception;

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    String callWithInputParams(@RestQuery String username) throws Exception;

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    Integer callWithOutputParams() throws Exception;

    @GET
    @Path("/return-data-result-set")
    @Produces(MediaType.APPLICATION_JSON)
    List<ReturnedUser> callReturningDataAsResultSet() throws Exception;

    @GET
    @Path("/return-data-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
    Integer callReturningDataAsBasicType() throws Exception;

    @GET
    @Path("/return-data-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserProfile> callReturningDataAsEntitiesNoAssociation() throws Exception;

    @GET
    @Path("/return-data-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    List<UserActivity> callReturningDataAsEntitiesWithToOne() throws Exception;

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    List<ReturnedUser> callWithCursor() throws Exception;
}
