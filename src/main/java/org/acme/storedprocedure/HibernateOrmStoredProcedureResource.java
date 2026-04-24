package org.acme.storedprocedure;

import jakarta.inject.Inject;
import jakarta.persistence.ParameterMode;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;
import org.jboss.resteasy.reactive.RestQuery;

import java.sql.Types;
import java.util.List;

@Path("/orm-sp")
@Transactional
public class HibernateOrmStoredProcedureResource implements StoredProcedureEndpoints {

    @Inject
    Session session;

    @Override
    public String profile() {
        return DatabaseProfileProducer.getDelegateName(session);
    }

    @Override
    public String callNoParams() {
        // NOTE: This could also be implemented as a native query call (createNativeQuery with CALL clause),
        // as long as there are no parameters or only input parameters.
        ProcedureCall call = session.createStoredProcedureCall("sp_add_activity");
        call.execute();
        return "Activity added via Hibernate ORM";
    }

    @Override
    public String callWithInputParams(String username) {
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

    @Override
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

    @Override
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callReturningDataAsResultSet() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                return session.createNativeQuery("SELECT * FROM sp_get_active_users_result_set()",
                                ReturnedUser.RESULT_SET_MAPPING, ReturnedUser.class)
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
            }
            default -> {
                // NOTE: This could also be implemented as a native query call (createNativeQuery with SELECT clause),
                // as long as there are no parameters or only input parameters.
                ProcedureCall call = session.createStoredProcedureCall("sp_get_active_users_result_set", ReturnedUser.RESULT_SET_MAPPING);
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
        }
    }

    @Override
    public Integer callReturningDataAsBasicType() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                // MSSQL requires an explicit schema here
                return session.createNativeQuery("SELECT dbo.sp_count_active_users_as_return()", Integer.class)
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getSingleResult();
            }
            default -> {
                // NOTE: This could also be implemented as a native query call (createNativeQuery with SELECT clause),
                // as long as there are no parameters or only input parameters.
                ProcedureCall call = session.createStoredProcedureCall("sp_count_active_users_as_return");
                call.markAsFunctionCall(Integer.class);
                // See callReturningDataAsResultSet for why this is sometimes necessary.
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                return (Integer) call.getSingleResult();
            }
        }
    }

    @Override
    public List<UserProfile> callReturningDataAsEntitiesNoAssociation() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                @SuppressWarnings("unchecked")
                List<UserProfile> results = session.createNativeQuery("SELECT * FROM sp_get_active_profiles()",
                                UserProfile.class)
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
                // The following are just checks and would not be present in an actual application.
                for (UserProfile result : results) {
                    // Entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.
                    assert session.contains(result);
                    // WARNING: to-many associations cannot be initialized eagerly from the result set returned by the procedure.
                    // Owned, to-one associations can be initialized through a custom mapping (like in callReturningDataAsEntitiesWithAssociation).
                    // With more Hibernate-native ways to retrieve entities -- e.g. JPQL/HQL queries, Criteria queries,
                    // or find()/findMultiple() with an entity graph -- any association could be fetched eagerly.
                    // Multiple SQL statements may be needed, but they can generally be batched (no N+1 select).
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
            default -> {
                // NOTE: This could also be implemented as a native query call (createNativeQuery with SELECT clause),
                // as long as there are no parameters or only input parameters.
                // WARNING: The stored procedure must return all columns needed to construct the entity instance;
                // missing columns will lead to "org.hibernate.exception.SQLGrammarException: Unable to find column position by name".
                ProcedureCall call = session.createStoredProcedureCall("sp_get_active_profiles", UserProfile.class);
                call.markAsFunctionCall(Types.REF_CURSOR);
                // See callReturningDataAsResultSet for why this is sometimes necessary.
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                @SuppressWarnings("unchecked")
                List<UserProfile> results = call.getResultList();
                // The following are just checks and would not be present in an actual application.
                for (UserProfile result : results) {
                    // Entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.
                    assert session.contains(result);
                    // WARNING: to-many associations cannot be initialized eagerly from the result set returned by the procedure.
                    // Owned, to-one associations can be initialized through a custom mapping (like in callReturningDataAsEntitiesWithAssociation).
                    // With more Hibernate-native ways to retrieve entities -- e.g. JPQL/HQL queries, Criteria queries,
                    // or find()/findMultiple() with an entity graph -- any association could be fetched eagerly.
                    // Multiple SQL statements may be needed, but they can generally be batched (no N+1 select).
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
        }
    }

    @Override
    public List<UserActivity> callReturningDataAsEntitiesWithToOne() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                @SuppressWarnings("unchecked")
                List<UserActivity> results = session.createNativeQuery("SELECT * FROM sp_get_activities_with_profiles()",
                                UserActivity.class,
                                UserActivity.RESULT_SET_MAPPING_WITH_PROFILE)
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
                // The following are just checks and would not be present in an actual application.
                for (UserActivity result : results) {
                    // Entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.
                    assert session.contains(result);
                    // WARNING: to-many associations cannot be initialized eagerly from the result set returned by the procedure.
                    // Owned, to-one associations can be initialized through a custom mapping (like in callReturningDataAsEntitiesWithAssociation).
                    // With more Hibernate-native ways to retrieve entities -- e.g. JPQL/HQL queries, Criteria queries,
                    // or find()/findMultiple() with an entity graph -- any association could be fetched eagerly.
                    // Multiple SQL statements may be needed, but they can generally be batched (no N+1 select).
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
            default -> {
                // NOTE: This could also be implemented as a native query call (createNativeQuery with SELECT clause),
                // as long as there are no parameters or only input parameters.
                // WARNING: The application is responsible for including all columns in the result set mapping.
                // Failing to do so will result in some data being nulled out, possibly leading to bugs.
                ProcedureCall call = session.createStoredProcedureCall("sp_get_activities_with_profiles", UserActivity.RESULT_SET_MAPPING_WITH_PROFILE);
                call.markAsFunctionCall(Types.REF_CURSOR);
                // See callReturningDataAsResultSet for why this is sometimes necessary.
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                @SuppressWarnings("unchecked")
                List<UserActivity> results = call.getResultList();
                // The following are just checks and would not be present in an actual application.
                for (UserActivity result : results) {
                    // Entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.
                    assert session.contains(result);
                    // WARNING: to-many associations cannot be initialized eagerly from the result set returned by the procedure.
                    // Owned, to-one associations can be initialized through a custom mapping (like in callReturningDataAsEntitiesWithAssociation).
                    // With more Hibernate-native ways to retrieve entities -- e.g. JPQL/HQL queries, Criteria queries,
                    // or find()/findMultiple() with an entity graph -- any association could be fetched eagerly.
                    // Multiple SQL statements may be needed, but they can generally be batched (no N+1 select).
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
        }
    }

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callWithCursor() {
        ProcedureCall call = session.createStoredProcedureCall("sp_get_users_cursor", ReturnedUser.RESULT_SET_MAPPING);
        if (DatabaseProfile.current() != DatabaseProfile.MSSQL) {
            // PostgreSQL and Oracle use REF_CURSOR output parameter
            call.registerStoredProcedureParameter(1, Class.class, ParameterMode.REF_CURSOR);
        }
        // MSSQL returns the result set directly (no REF_CURSOR parameter)
        // See callReturningDataAsResultSet for why this is sometimes necessary.
        call.addSynchronizedEntityClass(UserActivity.class);
        call.addSynchronizedEntityClass(UserProfile.class);
        return call.getResultList();
    }
}
