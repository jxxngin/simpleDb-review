import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sql {
    private final SimpleDB simpleDB;
    private final StringBuilder queryBuilder;
    private final List<Object> queryParams;
    private final Connection conn;

    public Sql(SimpleDB simpleDB) {
        this.simpleDB = simpleDB;
        try {
            this.conn = simpleDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        this.queryBuilder = new StringBuilder();
        this.queryParams = new ArrayList<>();
    }

    public Sql append(String query) {
        queryBuilder.append(query).append(" ");
        return this;
    }

    public Sql append(String query, Object... params) {
        queryBuilder.append(query).append(" ");
        queryParams.addAll(Arrays.asList(params));
        return this;
    }

    public Sql appendIn(String query, Object... params) {
        String inCluase = Arrays.stream(params)
                .map(o -> "?")
                .collect(Collectors.joining(", "));

        String replacedSql = query.replaceAll("\\?", inCluase);
        queryParams.addAll(Arrays.stream(params).toList());
        queryBuilder.append(replacedSql);

        return this;
    }

    private <T> T executeQuery(Function<ResultSet, T> handler, int... options) {
        String sql = queryBuilder.toString();
        try (
                PreparedStatement stmt = prepareStmt(conn, sql, options);
                ResultSet rs = stmt.executeQuery()
        ) {
            return handler.apply(rs);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private int executeUpdate(int... options) {
        String sql = queryBuilder.toString();

        try (PreparedStatement stmt = prepareStmt(conn, sql, options)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private PreparedStatement prepareStmt(Connection conn, String sql, int... options) throws SQLException {
        PreparedStatement stmt = options.length > 0
                ? conn.prepareStatement(sql, options[0])
                : conn.prepareStatement(sql);

        for (int i = 0; i < queryParams.size(); i++) {
            stmt.setObject(i + 1, queryParams.get(i));
        }

        return stmt;
    }

    public long insert() {
        String sql = queryBuilder.toString();

        try (PreparedStatement stmt = prepareStmt(conn, sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    public int update() {
        return executeUpdate();
    }

    public int delete() {
        return executeUpdate();
    }

    public <T> List<T> selectRows(Class<T> clazz) {
        String sql = queryBuilder.toString();
        List<T> rows = new ArrayList<>();

        try (
                PreparedStatement stmt = prepareStmt(conn, sql);
                ResultSet rs = stmt.executeQuery()
        ) {
            ResultSetMetaData metaData = rs.getMetaData();
            int colCount = metaData.getColumnCount();

            while (rs.next()) {
                if (clazz == Map.class) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        row.put(metaData.getColumnLabel(i), rs.getObject(i));
                    }
                    rows.add(clazz.cast(row));
                } else {
                    T obj = clazz.getDeclaredConstructor().newInstance();
                    for (int i = 1; i <= colCount; i++) {
                        String colName = metaData.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        Field field = clazz.getDeclaredField(colName);
                        field.setAccessible(true);
                        field.set(obj, val);
                    }
                    rows.add(obj);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return rows;
    }

    public <T> T selectRow(Class<T> clazz) {
        List<T> rows = selectRows(clazz);
        return rows.isEmpty() ? null : rows.get(0);
    }

    public List<Map<String, Object>> selectRows() {
        return selectRows(Map.class).stream()
                .map(row -> (Map<String, Object>) row)
                .collect(Collectors.toList());
    }

    public Map<String, Object> selectRow() {
        return selectRow(Map.class);
    }

    public LocalDateTime selectDatetime() {
        return executeQuery(rs -> {
            try {
                if (rs.next()) return rs.getObject(1, LocalDateTime.class);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public List<Long> selectLongs() {
        return executeQuery(rs -> {
            List<Long> ids = new ArrayList<>();
            try {
                while (rs.next()) ids.add(rs.getLong(1));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return ids;
        });
    }

    public Long selectLong() {
        return executeQuery(rs -> {
            try {
                if (rs.next()) return rs.getLong(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public String selectString() {
        return executeQuery(rs -> {
            try {
                if (rs.next()) return rs.getString(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public Boolean selectBoolean() {
        return executeQuery(rs -> {
            try {
                if (rs.next()) return rs.getBoolean(1);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}
