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
import org.acme.ReturnedUser;
import org.jboss.resteasy.reactive.RestQuery;

import java.util.List;

import static org.acme.Utils.notSupported;

@Path("/reactive-sp")
public class SqlReactiveClientStoredProcedureResource {

    @Inject
    Pool client;

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callNoParams() {
        return client.query("CALL sp_add_activity()")
                .execute()
                .map(rows -> "Activity added via Reactive SQL");
    }

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Uni<String> callWithInputParams(@RestQuery String username) {
        return client.preparedQuery("CALL sp_add_activity_with_user($1)")
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
    @Path("/return-data")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callReturningData() {
        return client.query("SELECT * FROM sp_get_active_users()")
                .mapping(row -> new ReturnedUser(row.getString("username"), row.getString("fullname")))
                .execute()
                .map(rows -> rows.stream().toList());
    }

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<ReturnedUser>> callWithCursor() {
        throw notSupported("Vert.x Reactive SQL Clients have no dedicated support for procedure calls, thus output parameters are not supported.");
    }
}
