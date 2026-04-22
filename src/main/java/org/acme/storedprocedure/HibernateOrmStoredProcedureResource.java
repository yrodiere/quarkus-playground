package org.acme.storedprocedure;

import jakarta.inject.Inject;
import jakarta.persistence.ParameterMode;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.ReturnedUser;
import org.acme.UserActivity;
import org.acme.UserProfile;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;
import org.jboss.resteasy.reactive.RestQuery;

import java.sql.Types;
import java.util.List;

@Path("/orm-sp")
@Transactional
public class HibernateOrmStoredProcedureResource {

    @Inject
    Session session;

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    public String callNoParams() {
        // NOTE: This could also be implemented as a native query call (createNativeQuery with CALL clause),
        // as long as there are no parameters or only input parameters.
        ProcedureCall call = session.createStoredProcedureCall("sp_add_activity");
        call.execute();
        return "Activity added via Hibernate ORM";
    }

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    public String callWithInputParams(@RestQuery String username) {
        // NOTE: This could also be implemented as a native query call (createNativeQuery with CALL clause),
        // as long as there are no parameters or only input parameters.
        ProcedureCall call = session.createStoredProcedureCall("sp_add_activity_with_user");
        call.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        call.setParameter(1, username);
        call.execute();
        // WARNING: like with any native calls, the persistence context may get out of sync as a result of this call.
        // For example if the corresponding UserProfile entity instance was already in the persistence context (session)
        // and its `activities` association was already loaded before calling the stored procedure,
        // that association would become out of sync with the DB (it would not contain the newly added activity).
        return "Activity added for user: " + username;
    }

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Integer callWithOutputParams() {
        ProcedureCall call = session.createStoredProcedureCall("sp_count_active_users");
        call.registerStoredProcedureParameter(1, Integer.class, ParameterMode.INOUT);
        call.setParameter(1, 0);
        // See callReturningData for why this is sometimes necessary.
        call.addSynchronizedEntityClass(UserActivity.class);
        call.addSynchronizedEntityClass(UserProfile.class);
        call.execute();
        return (Integer) call.getOutputParameterValue(1);
    }

    @GET
    @Path("/return-data")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callReturningData() {
        // NOTE: This could also be implemented as a native query call (createNativeQuery with SELECT clause),
        // as long as there are no parameters or only input parameters.
        ProcedureCall call = session.createStoredProcedureCall("sp_get_active_users", ReturnedUser.RESULT_SET_MAPPING);
        call.markAsFunctionCall(Types.REF_CURSOR);
        // WARNING: the persistence context might not have been flushed to the database,
        // which could cause the native call to return outdated information.
        // For example there might have been changes to UserActivity or UserProfile entities
        // in the session which have not been flushed and would affect the count of active users.
        // The following calls instruct Hibernate ORM to flush any such change before calling the procedure.
        call.addSynchronizedEntityClass(UserActivity.class);
        call.addSynchronizedEntityClass(UserProfile.class);
        return call.getResultList();
    }

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callWithCursor() {
        ProcedureCall call = session.createStoredProcedureCall("sp_get_users_cursor", ReturnedUser.RESULT_SET_MAPPING);
        call.registerStoredProcedureParameter(1, Class.class, ParameterMode.REF_CURSOR);
        // See callReturningData for why this is sometimes necessary.
        call.addSynchronizedEntityClass(UserActivity.class);
        call.addSynchronizedEntityClass(UserProfile.class);
        return call.getResultList();
    }
}
