import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.Map;

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

    public List<Map<String, Object>> selectRows() {
        String sql = queryBuilder.toString();
        List<Map<String, Object>> rows = new ArrayList<>();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();

                for (int i = 1; i <= colCount; i++) {
                    String colName = metaData.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(colName, val);
                }

                rows.add(row);
            }
        } catch (SQLException e) {
            System.out.println("selectRows() 실행 실패");
            e.printStackTrace();
        }

        return rows;
    }

    public Map<String, Object> selectRow() {
        String sql = queryBuilder.toString();
        Map<String, Object> row = new HashMap<>();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();

            if (rs.next()) {
                for (int i = 1; i <= colCount; i++) {
                    String colName = metaData.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    row.put(colName, val);
                }
            }

        } catch (SQLException e) {
            System.out.println("selectRow() 실행 실패");
            e.printStackTrace();
        }

        return row;
    }

    public LocalDateTime selectDatetime() {
        String sql = queryBuilder.toString();
        LocalDateTime date = null;

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            if (rs.next()) {
                date = rs.getObject(1, LocalDateTime.class);
            }
        } catch(SQLException e) {
            System.out.println("selectDatetime() 실행 실패");
            e.printStackTrace();
        }

        return date;
    }

    public Long selectLong() {
        String sql = queryBuilder.toString();
        Long id = null;

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
                ) {
            if (rs.next()) {
                id = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.out.println("selectLong() 실행 실패");
            e.printStackTrace();
        }

        return id;
    }

    public String selectString() {
        String sql = queryBuilder.toString();
        String title = new String();

        try (
                Connection conn = simpleDB.getConnection();
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
                ) {
            if (rs.next()) {
                title = rs.getString(1);
            }
        } catch (SQLException e) {
            System.out.println("selectString() 실행 실패");
            e.printStackTrace();
        }

        return title;
    }
}
