package org.acme.storedprocedure;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import org.acme.model.ReturnedUser;
import org.acme.model.UserActivity;
import org.acme.model.UserProfile;
import org.acme.scaffolding.DatabaseProfile;
import org.acme.scaffolding.DatabaseProfileProducer;

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

import static org.acme.scaffolding.Utils.notSupported;

@Path("/jdbc-sp")
@Transactional(rollbackOn = Exception.class)
public class SqlJdbcStoredProcedureResource implements StoredProcedureEndpoints {

    @Inject
    DataSource dataSource;

    @Override
    public String profile() {
        return DatabaseProfileProducer.getDelegateName(dataSource);
    }

    @Override
    public String callNoParams() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(switch (DatabaseProfile.current()) {
                case MSSQL -> "EXECUTE sp_add_activity";
                default -> "CALL sp_add_activity()";
            });
            return "Activity added via JDBC";
        }
    }

    @Override
    public String callWithInputParams(String username) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(switch (DatabaseProfile.current()) {
                 case MSSQL -> "EXECUTE sp_add_activity_with_user ?";
                 default -> "CALL sp_add_activity_with_user(?)";
             })) {
            stmt.setString(1, username);
            stmt.execute();
            return "Activity added for user: " + username;
        }
    }

    @Override
    public Integer callWithOutputParams() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall(
                     switch (DatabaseProfile.current()) {
                         case MSSQL -> "EXECUTE sp_count_active_users ?";
                         default -> "CALL sp_count_active_users(?)";
                     })) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.execute();
            return stmt.getInt(1);
        }
    }

    @Override
    public List<ReturnedUser> callReturningDataAsResultSet() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            switch (DatabaseProfile.current()) {
                case ORACLE -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call sp_get_active_users_result_set() }")) {
                        stmt.registerOutParameter(1, Types.REF_CURSOR);
                        stmt.execute();
                        return toReturnedUsers((ResultSet) stmt.getObject(1));
                    }
                }
                default -> {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM sp_get_active_users_result_set()")) {
                        return toReturnedUsers(rs);
                    }
                }
            }
        }
    }

    private List<ReturnedUser> toReturnedUsers(ResultSet rs) throws SQLException {
        List<ReturnedUser> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new ReturnedUser(
                    rs.getString("username"),
                    rs.getString("fullname")
            ));
        }
        return results;
    }

    @Override
    public Integer callReturningDataAsBasicType() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(switch (DatabaseProfile.current()) {
                 // MSSQL requires an explicit schema here
                 case MSSQL -> "SELECT dbo.sp_count_active_users_as_return()";
                 default -> "SELECT sp_count_active_users_as_return()";
             })) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public List<UserProfile> callReturningDataAsEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
    }

    @Override
    public List<UserActivity> callReturningDataAsEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
    }

    @Override
    public List<ReturnedUser> callWithCursor() throws SQLException {
        if (DatabaseProfile.current() == DatabaseProfile.MSSQL) {
            throw notSupported("The MSSQL JDBC driver does not support cursors in output parameters.");
        }
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
