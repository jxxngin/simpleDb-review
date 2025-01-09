import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import java.sql.Statement;
import java.sql.PreparedStatement;

public class Sql {
    private final SimpleDB simpleDB;
    private final StringBuilder queryBuilder;
    private final List<Object> queryParams;

    public Sql(SimpleDB simpleDB) {
        this.simpleDB = simpleDB;
        this.queryBuilder = new StringBuilder();
        this.queryParams = new ArrayList<>();
    }

    public Sql append(String query) {
        queryBuilder.append(query).append(" ");
        return this;
    }

    public Sql append(String query, Object... params) {
        queryBuilder.append(query).append(" ");
        for (Object param : params) {
            queryParams.add(param);
        }
        return this;
    }

    private PreparedStatement prepareStmt(Connection conn, String sql, int... option) throws SQLException {
        PreparedStatement stmt;

        if (option.length > 0) {
            stmt = conn.prepareStatement(sql, option[0]);
        } else {
            stmt = conn.prepareStatement(sql);
        }

        for (int i = 0; i < queryParams.size(); i++) {
            stmt.setObject(i + 1, queryParams.get(i));
        }

        return stmt;
    }

    public long insert() {
        String sql = queryBuilder.toString();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            System.out.println("insert() 실행 실패");
            e.printStackTrace();
        }

        return -1;
    }

    public int update() {
        String sql = queryBuilder.toString();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql)
        ) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("update() 실행 실패");
            e.printStackTrace();
        }

        return 0;
    }

    public int delete() {
        String sql = queryBuilder.toString();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql)
        ) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("delete() 실행 실패");
            e.printStackTrace();
        }

        return 0;
    }
}
