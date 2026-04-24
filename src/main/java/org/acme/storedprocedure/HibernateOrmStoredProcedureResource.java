package org.acme.storedprocedure;

import jakarta.inject.Inject;
import jakarta.persistence.ParameterMode;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

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
    public String callProcedureWithoutParams() {
        // This could simply be a native query execution; see README section "Calling simple procedures"
        ProcedureCall call = session.createStoredProcedureCall("sp_add_activity");
        call.execute();
        return "Activity added via Hibernate ORM";
    }

    @Override
    public String callProcedureWithInputParams(String username) {
        // This could simply be a native query execution; see README section "Calling simple procedures"
        ProcedureCall call = session.createStoredProcedureCall("sp_add_activity_with_user");
        call.registerStoredProcedureParameter(1, String.class, ParameterMode.IN);
        call.setParameter(1, username);
        call.execute();
        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
        return "Activity added for user: " + username;
    }

    // ===== Functions (with return values) =====

    @Override
    public Integer callFunctionReturningBasicType() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                // MSSQL requires an explicit schema here.
                return session.createNativeQuery("SELECT dbo.fn_count_active_users()", Integer.class)
                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getSingleResult();
            }
            default -> {
                // This could simply be a native query execution; see README section "Calling simple procedures"
                ProcedureCall call = session.createStoredProcedureCall("fn_count_active_users");
                call.markAsFunctionCall(Integer.class);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                return (Integer) call.getSingleResult();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callFunctionReturningTuples() {
        // This could simply be a native query execution; see README section "Calling simple procedures"
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                return session.createNativeQuery("SELECT * FROM fn_get_active_users()",
                                ReturnedUser.RESULT_SET_MAPPING, ReturnedUser.class)
                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
            }
            default -> {
                ProcedureCall call = session.createStoredProcedureCall("fn_get_active_users", ReturnedUser.RESULT_SET_MAPPING);
                call.markAsFunctionCall(Types.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                return call.getResultList();
            }
        }
    }


    @Override
    public List<UserProfile> callFunctionReturningEntitiesNoAssociation() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                @SuppressWarnings("unchecked")
                List<UserProfile> results = session.createNativeQuery("SELECT * FROM fn_get_active_profiles()",
                                UserProfile.class)
                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserProfile result : results) {
                    assert session.contains(result);
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
            default -> {
                ProcedureCall call = session.createStoredProcedureCall("fn_get_active_profiles", UserProfile.class);
                call.markAsFunctionCall(Types.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                @SuppressWarnings("unchecked")
                List<UserProfile> results = call.getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserProfile result : results) {
                    assert session.contains(result);
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
        }
    }

    @Override
    public List<UserActivity> callFunctionReturningEntitiesWithToOne() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // A MSSQL FUNCTION can only be executed as part of an SQL query.
                @SuppressWarnings("unchecked")
                List<UserActivity> results = session.createNativeQuery("SELECT * FROM fn_get_activities_with_profiles()",
                                UserActivity.class,
                                UserActivity.RESULT_SET_MAPPING_WITH_PROFILE)
                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserActivity result : results) {
                    assert session.contains(result);
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
            default -> {
                ProcedureCall call = session.createStoredProcedureCall("fn_get_activities_with_profiles", UserActivity.RESULT_SET_MAPPING_WITH_PROFILE);
                call.markAsFunctionCall(Types.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                @SuppressWarnings("unchecked")
                List<UserActivity> results = call.getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserActivity result : results) {
                    assert session.contains(result);
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
        }
    }

    // ===== Procedures (with output parameters) =====

    @Override
    public Integer callProcedureWithOutputParamBasicType() {
        ProcedureCall call = session.createStoredProcedureCall("sp_count_active_users");
        call.registerStoredProcedureParameter(1, Integer.class, ParameterMode.OUT);
        call.setParameter(1, 0);
        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
        call.addSynchronizedEntityClass(UserActivity.class);
        call.addSynchronizedEntityClass(UserProfile.class);
        call.execute();
        return (Integer) call.getOutputParameterValue(1);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ReturnedUser> callProcedureWithOutputParamTuples() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // MSSQL doesn't support REF_CURSOR, returns result set directly; see README section "Calling more complex procedures"
                return session.createStoredProcedureQuery("sp_get_active_users", ReturnedUser.RESULT_SET_MAPPING)
                        .getResultList();
            }
            default -> {
                // PostgreSQL and Oracle use REF_CURSOR output parameter
                ProcedureCall call = session.createStoredProcedureCall("sp_get_active_users", ReturnedUser.RESULT_SET_MAPPING);
                call.registerStoredProcedureParameter(1, Class.class, ParameterMode.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                return call.getResultList();
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserProfile> callProcedureWithOutputParamEntitiesNoAssociation() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // MSSQL doesn't support REF_CURSOR, returns result set directly; see README section "Calling more complex procedures"
                List<UserProfile> results = session.createStoredProcedureQuery("sp_get_active_profiles", UserProfile.class)
                        .getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserProfile result : results) {
                    assert session.contains(result);
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
            default -> {
                // PostgreSQL and Oracle use REF_CURSOR output parameter
                ProcedureCall call = session.createStoredProcedureCall("sp_get_active_profiles", UserProfile.class);
                call.registerStoredProcedureParameter(1, Class.class, ParameterMode.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                List<UserProfile> results = call.getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserProfile result : results) {
                    assert session.contains(result);
                    assert !Hibernate.isInitialized(result.activities);
                }
                return results;
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserActivity> callProcedureWithOutputParamEntitiesWithToOne() {
        switch (DatabaseProfile.current()) {
            case MSSQL -> {
                // MSSQL doesn't support REF_CURSOR, returns result set directly; see README section "Calling more complex procedures"
                List<UserActivity> results = session.createNativeQuery("EXECUTE sp_get_activities_with_profiles",
                                UserActivity.class,
                                UserActivity.RESULT_SET_MAPPING_WITH_PROFILE)
                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                        .addSynchronizedEntityClass(UserActivity.class)
                        .addSynchronizedEntityClass(UserProfile.class)
                        .getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserActivity result : results) {
                    assert session.contains(result);
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
            default -> {
                // PostgreSQL and Oracle use REF_CURSOR output parameter
                ProcedureCall call = session.createStoredProcedureCall("sp_get_activities_with_profiles", UserActivity.RESULT_SET_MAPPING_WITH_PROFILE);
                call.registerStoredProcedureParameter(1, Class.class, ParameterMode.REF_CURSOR);
                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                call.addSynchronizedEntityClass(UserActivity.class);
                call.addSynchronizedEntityClass(UserProfile.class);
                List<UserActivity> results = call.getResultList();
                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                for (UserActivity result : results) {
                    assert session.contains(result);
                    assert Hibernate.isInitialized(result.profile);
                    assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                }
                return results;
            }
        }
    }
}
