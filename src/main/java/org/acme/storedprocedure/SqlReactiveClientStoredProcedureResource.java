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
    public Uni<String> callNoParams() {
        return client.query(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity";
                    default -> "CALL sp_add_activity()";
                })
                .execute()
                .map(rows -> "Activity added via Reactive SQL");
    }

    @Override
    public Uni<String> callWithInputParams(String username) {
        return client.preparedQuery(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity_with_user @p1";
                    case ORACLE -> "CALL sp_add_activity_with_user(?)";
                    default -> "CALL sp_add_activity_with_user($1)";
                })
                .execute(Tuple.of(username))
                .map(rows -> "Activity added for user: " + username);
    }

    @Override
    public Uni<Integer> callWithOutputParams() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus output parameters are not supported.");
    }

    @Override
    public Uni<List<ReturnedUser>> callReturningDataAsResultSet() {
        if (DatabaseProfile.current() == DatabaseProfile.ORACLE) {
            throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported.");
        }
        return client.query("SELECT * FROM sp_get_active_users_result_set()")
                .mapping(row -> new ReturnedUser(row.getString("username"), row.getString("fullname")))
                .execute()
                .map(rows -> rows.stream().toList());
    }

    @Override
    public Uni<Integer> callReturningDataAsBasicType() {
        return client.query(switch (DatabaseProfile.current()) {
                    // MSSQL requires an explicit schema here
                    case MSSQL -> "SELECT dbo.sp_count_active_users_as_return()";
                    default -> "SELECT sp_count_active_users_as_return()";
                })
                .execute()
                .map(rows -> {
                    var iterator = rows.iterator();
                    return iterator.hasNext() ? iterator.next().getInteger(0) : 0;
                });
    }

    @Override
    public Uni<List<UserProfile>> callReturningDataAsEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }

    @Override
    public Uni<List<UserActivity>> callReturningDataAsEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }


    @Override
    public Uni<List<ReturnedUser>> callWithCursor() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus output parameters are not supported.");
    }
}
