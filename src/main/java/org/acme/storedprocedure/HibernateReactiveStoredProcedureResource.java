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
    public Uni<String> callNoParams() {
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
    public Uni<String> callWithInputParams(String username) {
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

    @Override
    public Uni<Integer> callWithOutputParams() {
        throw notSupported("Hibernate Reactive has no dedicated support for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<ReturnedUser>> callReturningDataAsResultSet() {
        if (DatabaseProfile.current() == DatabaseProfile.ORACLE) {
            throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported.");
        }
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery("SELECT * FROM sp_get_active_users_result_set()",
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

    @Override
    public Uni<Integer> callReturningDataAsBasicType() {
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery(switch (DatabaseProfile.current()) {
                            // MSSQL requires an explicit schema here
                            case MSSQL -> "SELECT dbo.sp_count_active_users_as_return()";
                            default -> "SELECT sp_count_active_users_as_return()";
                        }, Integer.class,
                                // See callReturningDataAsResultSet for why this is sometimes necessary.
                                new AffectedEntities(UserActivity.class, UserProfile.class))
                        .getSingleResult()
        );
    }

    @Override
    public Uni<List<UserProfile>> callReturningDataAsEntitiesNoAssociation() {
        if (DatabaseProfile.current() == DatabaseProfile.ORACLE) {
            throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported.");
        }
        // WARNING: The stored procedure must return all columns needed to construct the entity instance;
        // missing columns will lead to "org.hibernate.exception.SQLGrammarException: Unable to find column position by name".
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery("SELECT * FROM sp_get_active_profiles()", UserProfile.class,
                                // See callReturningDataAsResultSet for why this is sometimes necessary.
                                new AffectedEntities(UserActivity.class, UserProfile.class))
                        .getResultList()
                        // The following are just checks and would not be present in an actual application.
                        .map(results -> {
                            for (UserProfile result : results) {
                                // Entities are attached to the session, allowing lazy-loading, dirty-tracking, etc.
                                assert session.contains(result);
                                // WARNING: unowned or to-many associations cannot be initialized eagerly from the result set returned by the procedure.
                                // Owned to-one associations can be initialized through a custom result set mapping:
                                // see https://docs.hibernate.org/orm/7.3/userguide/html_single/#sql-entity-named-queries
                                assert !Hibernate.isInitialized(result.activities);
                            }
                            return results;
                        })
        );
    }

    @Override
    public Uni<List<UserActivity>> callReturningDataAsEntitiesWithToOne() {
        if (DatabaseProfile.current() == DatabaseProfile.ORACLE) {
            throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported.");
        }
        if (true) {
            throw notSupported("See https://github.com/hibernate/hibernate-reactive/issues/3616");
        }
        // WARNING: The stored procedure must return all columns needed to construct the entity instance;
        // missing columns will lead to "org.hibernate.exception.SQLGrammarException: Unable to find column position by name".
        return sessionFactory.withTransaction(session ->
                session.createNativeQuery("SELECT * FROM sp_get_activities_with_profiles()",
                                session.getResultSetMapping(UserActivity.class, UserActivity.RESULT_SET_MAPPING_WITH_PROFILE),
                                // See callReturningDataAsResultSet for why this is sometimes necessary.
                                new AffectedEntities(UserActivity.class, UserProfile.class))
                        .getResultList()
                        // The following are just checks and would not be present in an actual application.
                        .map(results -> {
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
                        })
        );
    }

    @Override
    public Uni<List<ReturnedUser>> callWithCursor() {
        throw notSupported("Hibernate Reactive has no dedicated support for procedure calls, thus output parameters are not supported.");
    }
}
