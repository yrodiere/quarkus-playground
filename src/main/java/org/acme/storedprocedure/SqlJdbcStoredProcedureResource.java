package org.acme.storedprocedure;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.ReturnedUser;
import org.jboss.resteasy.reactive.RestQuery;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

@Path("/jdbc-sp")
@Transactional(rollbackOn = Exception.class)
public class SqlJdbcStoredProcedureResource {

    @Inject
    DataSource dataSource;

    @POST
    @Path("/no-params")
    @Produces(MediaType.TEXT_PLAIN)
    public String callNoParams() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CALL sp_add_activity()");
            return "Activity added via JDBC";
        }
    }

    @POST
    @Path("/input-params")
    @Produces(MediaType.TEXT_PLAIN)
    public String callWithInputParams(@RestQuery String username) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("CALL sp_add_activity_with_user(?)")) {
            stmt.setString(1, username);
            stmt.execute();
            return "Activity added for user: " + username;
        }
    }

    @GET
    @Path("/output-params")
    @Produces(MediaType.TEXT_PLAIN)
    public Integer callWithOutputParams() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_count_active_users(?)")) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(1);
        }
    }

    @GET
    @Path("/return-data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReturnedUser> callReturningData() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM sp_get_active_users()")) {

            List<ReturnedUser> results = new ArrayList<>();
            while (rs.next()) {
                results.add(new ReturnedUser(
                        rs.getString("username"),
                        rs.getString("fullname")
                ));
            }
            return results;
        }
    }

    @GET
    @Path("/cursor")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ReturnedUser> callWithCursor() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("CALL sp_get_users_cursor(?)")) {
            stmt.registerOutParameter(1, Types.REF_CURSOR);
            stmt.execute();

            List<ReturnedUser> results = new ArrayList<>();
            try (ResultSet rs = (ResultSet) stmt.getObject(1)) {
                while (rs.next()) {
                    results.add(new ReturnedUser(
                            rs.getString("username"),
                            rs.getString("fullname")
                    ));
                }
            }
            return results;
        }
    }
}
