package org.acme.storedprocedure;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;

import java.util.List;

import static org.acme.scaffolding.Utils.notSupported;

@Path("/reactive-sp")
public class SqlReactiveClientStoredProcedureResource implements ReactiveStoredProcedureEndpoints {

    @Inject
    Pool client;

    @Override
    public String profile() {
        return DatabaseProfileProducer.getDelegateName(client);
    }

    @Override
    public Uni<String> callProcedureWithoutParams() {
        return client.query(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity";
                    default -> "CALL sp_add_activity()";
                })
                .execute()
                .map(rows -> "Activity added via Reactive SQL");
    }

    @Override
    public Uni<String> callProcedureWithInputParams(String username) {
        return client.preparedQuery(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity_with_user @p1";
                    case ORACLE -> "CALL sp_add_activity_with_user(?)";
                    default -> "CALL sp_add_activity_with_user($1)";
                })
                .execute(Tuple.of(username))
                .map(rows -> "Activity added for user: " + username);
    }

    // ===== Functions (with return values) =====

    @Override
    public Uni<Integer> callFunctionReturningBasicType() {
        return client.query(switch (DatabaseProfile.current()) {
                    // MSSQL requires an explicit schema here
                    case MSSQL -> "SELECT dbo.fn_count_active_users()";
                    default -> "SELECT fn_count_active_users()";
                })
                .execute()
                .map(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? iterator.next().getInteger(0) : 0;
                });
    }

    @Override
    public Uni<List<ReturnedUser>> callFunctionReturningTuples() {
        switch (DatabaseProfile.current()) {
            case ORACLE -> {
                // Vert.x Reactive SQL Clients have no dedicated API for procedure calls, thus Oracle's cursor-returning functions are not supported
                throw notSupported("Vert.x Reactive SQL Clients have no dedicated API for procedure calls, thus Oracle's cursor-returning functions are not supported.");
            }
            default -> {
                return client.query("SELECT * FROM fn_get_active_users()")
                        .mapping(row -> new ReturnedUser(row.getString("username"), row.getString("fullname")))
                        .execute()
                        .map(rows -> rows.stream().toList());
            }
        }
    }

    @Override
    public Uni<List<UserProfile>> callFunctionReturningEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }

    @Override
    public Uni<List<UserActivity>> callFunctionReturningEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }

    // ===== Procedures (with output parameters) =====

    @Override
    public Uni<Integer> callProcedureWithOutputParamBasicType() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated API for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<ReturnedUser>> callProcedureWithOutputParamTuples() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated API for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<UserProfile>> callProcedureWithOutputParamEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }

    @Override
    public Uni<List<UserActivity>> callProcedureWithOutputParamEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }
}
