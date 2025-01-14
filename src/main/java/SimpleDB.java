import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDB {
    private static String DB_PROTOCOL = "jdbc:mysql://";

    private String host;
    private String username;
    private String password;
    private String project;

    private String mode = "none";
    private static final ThreadLocal<Connection> threadLocalConnection = new ThreadLocal<>();

    public SimpleDB(String host, String username, String password, String project) {
        this.host = host;
        this.username = username;
        this.password = password;
        this.project = project;
    }

    public void setDevMode(boolean value) {
        mode = value ? "dev" : "none";
    }

    public Connection getConnection() throws SQLException {
        Connection conn = threadLocalConnection.get();
        if (conn == null || conn.isClosed()) {
            String url = DB_PROTOCOL + host + "/" + project;
            conn = DriverManager.getConnection(url, username, password);
            threadLocalConnection.set(conn);
        }
        return conn;
    }

    public void run(String query) {
        try (var stmt = getConnection().createStatement()) {
            stmt.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String query, String title, String body, boolean isBlind) {
        try (var stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, title);
            stmt.setString(2, body);
            stmt.setBoolean(3, isBlind);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Sql genSql() {
        return new Sql(this);
    }

    public void close() {
        try {
            Connection conn = threadLocalConnection.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
            threadLocalConnection.remove();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void startTransaction() {
        try {
            getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback() {
        try {
            Connection conn = getConnection();
            conn.rollback();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void commit() {
        try {
            Connection conn = getConnection();
            conn.commit();
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}