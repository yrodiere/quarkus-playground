package org.acme.storedprocedure;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.ReturnedUser;
import org.acme.UserActivity;
import org.acme.UserProfile;
import org.hibernate.reactive.common.AffectedEntities;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static org.acme.Utils.notSupported;

@Path("/reactive-orm-sp")
public class HibernateReactiveStoredProcedureResource {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callNoParams() {
        return sessionFactory.withTransaction((session, tx) ->
                session.createNativeQuery("CALL sp_add_activity()")
                        .executeUpdate()
                        .replaceWith("Activity added via Hibernate Reactive")
        );
    }

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callWithInputParams(@RestQuery String username) {
        return sessionFactory.withTransaction((session, tx) ->
                session.createNativeQuery("CALL sp_add_activity_with_user(:username)")
                        .setParameter("username", username)
                        .executeUpdate()
                        .replaceWith("Activity added for user: " + username)
        );
    }

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Integer> callWithOutputParams() {
        throw notSupported("Hibernate Reactive has no dedicated support for procedure calls, thus output parameters are not supported.");
    }

    @GET
    @Path("/return-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callReturningData() {
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery("SELECT * FROM sp_get_active_users()",
                                session.getResultSetMapping(ReturnedUser.class, ReturnedUser.RESULT_SET_MAPPING),
                                // WARNING: the persistence context might not have been flushed to the database,
                                // which could cause the native call to return outdated information.
                                // For example there might have been changes to UserActivity or UserProfile entities
                                // in the session which have not been flushed and would affect the count of active users.
                                // The following argument instructs Hibernate Reactive to flush any such change before calling the procedure.
                                new AffectedEntities(UserActivity.class, UserProfile.class))
                        .getResultList()
        );
    }

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callWithCursor() {
        throw notSupported("Hibernate Reactive has no dedicated support for procedure calls, thus output parameters are not supported.");
    }
}
