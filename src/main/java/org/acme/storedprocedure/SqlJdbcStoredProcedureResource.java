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
    public String callProcedureWithoutParams() throws SQLException {
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
    public String callProcedureWithInputParams(String username) throws SQLException {
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

    // ===== Functions (with return values) =====

    @Override
    public Integer callFunctionReturningBasicType() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(switch (DatabaseProfile.current()) {
                 // MSSQL requires an explicit schema here
                 case MSSQL -> "SELECT dbo.fn_count_active_users()";
                 default -> "SELECT fn_count_active_users()";
             })) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    @Override
    public List<ReturnedUser> callFunctionReturningTuples() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            switch (DatabaseProfile.current()) {
                case ORACLE -> {
                    try (CallableStatement stmt = conn.prepareCall("{ ? = call fn_get_active_users() }")) {
                        stmt.registerOutParameter(1, Types.REF_CURSOR);
                        stmt.execute();
                        return toReturnedUsers((ResultSet) stmt.getObject(1));
                    }
                }
                default -> {
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM fn_get_active_users()")) {
                        return toReturnedUsers(rs);
                    }
                }
            }
        }
    }

    @Override
    public List<UserProfile> callFunctionReturningEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
    }

    @Override
    public List<UserActivity> callFunctionReturningEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
    }

    // ===== Procedures (with output parameters) =====

    @Override
    public Integer callProcedureWithOutputParamBasicType() throws SQLException {
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
    public List<ReturnedUser> callProcedureWithOutputParamTuples() throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            switch (DatabaseProfile.current()) {
                case MSSQL -> {
                    // MSSQL doesn't support REF_CURSOR, returns result set directly; see README section "Calling more complex procedures"
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("EXECUTE sp_get_active_users")) {
                        return toReturnedUsers(rs);
                    }
                }
                default -> {
                    // PostgreSQL and Oracle use REF_CURSOR output parameter
                    try (CallableStatement stmt = conn.prepareCall("CALL sp_get_active_users(?)")) {
                        stmt.registerOutParameter(1, Types.REF_CURSOR);
                        stmt.execute();
                        return toReturnedUsers((ResultSet) stmt.getObject(1));
                    }
                }
            }
        }
    }

    @Override
    public List<UserProfile> callProcedureWithOutputParamEntitiesNoAssociation() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
    }

    @Override
    public List<UserActivity> callProcedureWithOutputParamEntitiesWithToOne() {
        throw notSupported("Entities and persistence context do not make sense with raw JDBC");
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
}
