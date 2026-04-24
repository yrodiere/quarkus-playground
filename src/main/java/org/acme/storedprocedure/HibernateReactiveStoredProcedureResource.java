package org.acme.storedprocedure;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;
import org.hibernate.Hibernate;
import org.hibernate.reactive.common.AffectedEntities;
import org.hibernate.reactive.mutiny.Mutiny;

import java.util.List;

import static org.acme.scaffolding.Utils.notSupported;

@Path("/reactive-orm-sp")
public class HibernateReactiveStoredProcedureResource implements ReactiveStoredProcedureEndpoints {

    @Inject
    Mutiny.SessionFactory sessionFactory;

    @Override
    public String profile() {
        return DatabaseProfileProducer.getDelegateName(sessionFactory);
    }

    @Override
    public Uni<String> callProcedureWithoutParams() {
        return sessionFactory.withTransaction((session, tx) ->
                session.createNativeQuery(switch (DatabaseProfile.current()) {
                            case MSSQL -> "EXECUTE sp_add_activity";
                            default -> "CALL sp_add_activity()";
                        })
                        .executeUpdate()
                        .replaceWith("Activity added via Hibernate Reactive")
        );
    }

    @Override
    public Uni<String> callProcedureWithInputParams(String username) {
        return sessionFactory.withTransaction((session, tx) ->
                session.createNativeQuery(switch (DatabaseProfile.current()) {
                            case MSSQL -> "EXECUTE sp_add_activity_with_user :username";
                            default -> "CALL sp_add_activity_with_user(:username)";
                        })
                        .setParameter("username", username)
                        .executeUpdate()
                        .replaceWith("Activity added for user: " + username)
        );
    }

    // ===== Functions (with return values) =====

    @Override
    public Uni<Integer> callFunctionReturningBasicType() {
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery(switch (DatabaseProfile.current()) {
                                    // MSSQL requires an explicit schema here
                                    case MSSQL -> "SELECT dbo.fn_count_active_users()";
                                    default -> "SELECT fn_count_active_users()";
                                }, Integer.class,
                                // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                                new AffectedEntities(UserActivity.class, UserProfile.class))
                        .getSingleResult()
        );
    }

    @Override
    public Uni<List<ReturnedUser>> callFunctionReturningTuples() {
        switch (DatabaseProfile.current()) {
            case ORACLE -> {
                throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus Oracle's cursor-returning functions are not supported.");
            }
            default -> {
                return sessionFactory.withTransaction(session ->
                        session.createNativeQuery("SELECT * FROM fn_get_active_users()",
                                        session.getResultSetMapping(ReturnedUser.class, ReturnedUser.RESULT_SET_MAPPING),
                                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                                        new AffectedEntities(UserActivity.class, UserProfile.class))
                                .getResultList()
                );
            }
        }
    }

    @Override
    public Uni<List<UserProfile>> callFunctionReturningEntitiesNoAssociation() {
        switch (DatabaseProfile.current()) {
            case ORACLE -> {
                throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus Oracle's cursor-returning functions are not supported.");
            }
            default -> {
                return sessionFactory.withTransaction(session ->
                        session.createNativeQuery("SELECT * FROM fn_get_active_profiles()", UserProfile.class,
                                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                                        new AffectedEntities(UserActivity.class, UserProfile.class))
                                .getResultList()
                                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                                .map(results -> {
                                    for (UserProfile result : results) {
                                        assert session.contains(result);
                                        assert !Hibernate.isInitialized(result.activities);
                                    }
                                    return results;
                                })
                );
            }
        }
    }

    @Override
    public Uni<List<UserActivity>> callFunctionReturningEntitiesWithToOne() {
        switch (DatabaseProfile.current()) {
            case ORACLE -> {
                throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus Oracle's cursor-returning functions are not supported.");
            }
            default -> {
                if (true) {
                    throw notSupported("See https://github.com/hibernate/hibernate-reactive/issues/3616");
                }
                return sessionFactory.withTransaction(session ->
                        session.createNativeQuery("SELECT * FROM fn_get_activities_with_profiles()",
                                        session.getResultSetMapping(UserActivity.class, UserActivity.RESULT_SET_MAPPING_WITH_PROFILE),
                                        // This is sometimes necessary, but not always; see README section "Persistence context synchronization"
                                        new AffectedEntities(UserActivity.class, UserProfile.class))
                                .getResultList()
                                // The following are checks demonstrating README section "Entity mapping completeness and persistence context"
                                .map(results -> {
                                    for (UserActivity result : results) {
                                        assert session.contains(result);
                                        assert Hibernate.isInitialized(result.profile);
                                        assert Hibernate.isPropertyInitialized(result.profile, "fullName");
                                    }
                                    return results;
                                })
                );
            }
        }
    }

    // ===== Procedures (with output parameters) =====

    @Override
    public Uni<Integer> callProcedureWithOutputParamBasicType() {
        throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<ReturnedUser>> callProcedureWithOutputParamTuples() {
        throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<UserProfile>> callProcedureWithOutputParamEntitiesNoAssociation() {
        throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<UserActivity>> callProcedureWithOutputParamEntitiesWithToOne() {
        throw notSupported("Hibernate Reactive has no dedicated API for procedure calls, thus output parameters are not supported.");
    }
}
