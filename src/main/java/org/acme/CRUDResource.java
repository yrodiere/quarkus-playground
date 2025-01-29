package org.acme;

import io.agroal.pool.wrapper.ConnectionWrapper;
import io.quarkus.arc.Arc;
import io.quarkus.arc.ClientProxy;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestQuery;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;

;

@Path("/crud")
@Transactional
public class CRUDResource {

    void startupChecks(@Observes StartupEvent event) {
        var datasource = ClientProxy.unwrap(Arc.container().instance(DataSource.class).get());
        try (var connection = datasource.getConnection()) {
            var rawConnection = ((ConnectionWrapper) connection).getHandler().rawConnection();
            Log.infof("Using connection type: %s", rawConnection.getClass());
            if (!(rawConnection instanceof oracle.ucp.jdbc.proxy.oracle.ConnectionProxy)) {
                throw new IllegalStateException("Connection type does not extend " + oracle.ucp.jdbc.proxy.oracle.ConnectionProxy.class.getName()
                        + "; it is of type " + rawConnection.getClass().getName() + "instead.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @POST
    @Produces(MediaType.TEXT_PLAIN)
    public Response create(@RestQuery String value) throws URISyntaxException {
        var entity = new MyEntity();
        entity.field = value;
        entity.persist();
        var id = entity.id.toString();
        return Response.created(new URI("/crud/" + id)).entity(id).build();
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    public String retrieve(@RestPath Long id) {
        MyEntity entity = MyEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity.field;
    }

    @PUT
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    public void update(@RestPath Long id, @RestQuery String value) {
        MyEntity entity = MyEntity.findById(id);
        if (entity == null) {
            throw new NotFoundException();
        }
        entity.field = value;
    }

    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/{id}")
    public void delete(@RestPath Long id) {
        MyEntity entity = MyEntity.findById(id);
        if (entity == null) {
            return;
        } else {
            entity.delete();
        }
    }
}
