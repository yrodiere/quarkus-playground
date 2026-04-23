package org.acme.storedprocedure;

import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static org.acme.scaffolding.Utils.notSupported;

@Path("/reactive-sp")
public class SqlReactiveClientStoredProcedureResource {

    @Inject
    Pool client;

    @GET
    @Path("/profile")
    @Produces(MediaType.TEXT_PLAIN)
    public String profile() {
        return DatabaseProfileProducer.getDelegateName(client);
    }

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callNoParams() {
        return client.query(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity";
                    default -> "CALL sp_add_activity()";
                })
                .execute()
                .map(rows -> "Activity added via Reactive SQL");
    }

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callWithInputParams(@RestQuery String username) {
        return client.preparedQuery(switch (DatabaseProfile.current()) {
                    case MSSQL -> "EXECUTE sp_add_activity_with_user @p1";
                    case ORACLE -> "CALL sp_add_activity_with_user(?)";
                    default -> "CALL sp_add_activity_with_user($1)";
                })
                .execute(Tuple.of(username))
                .map(rows -> "Activity added for user: " + username);
    }

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<Integer> callWithOutputParams() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus output parameters are not supported.");
    }

    @GET
    @Path("/return-data-result-set")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callReturningDataAsResultSet() {
        if (DatabaseProfile.current() == DatabaseProfile.ORACLE) {
            throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus Oracle's cursor-returning functions are not supported.");
        }
        return client.query("SELECT * FROM sp_get_active_users_result_set()")
                .mapping(row -> new ReturnedUser(row.getString("username"), row.getString("fullname")))
                .execute()
                .map(rows -> rows.stream().toList());
    }

    @GET
    @Path("/return-data-basic-type")
    @Produces(MediaType.TEXT_PLAIN)
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

    @GET
    @Path("/return-data-entities-no-association")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserActivity> callReturningDataAsEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }

    @GET
    @Path("/return-data-entities-toone")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserActivity> callReturningDataAsEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw Vert.x Reactive SQL clients");
    }


    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callWithCursor() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus output parameters are not supported.");
    }
}
