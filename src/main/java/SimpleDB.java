import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleDB {
    private static final String DB_PROTOCOL = "jdbc:mysql://";

    private final String host;
    private final String username;
    private final String password;
    private final String project;

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
        try (
                var stmt = getConnection().createStatement()
        ) {
            stmt.execute(query);
        } catch (SQLException e) {
            throw new RuntimeException("run() 실패", e);
        }
    }

    public void run(String query, String title, String body, boolean isBlind) {
        try (
                var stmt = getConnection().prepareStatement(query)
        ) {
            stmt.setString(1, title);
            stmt.setString(2, body);
            stmt.setBoolean(3, isBlind);

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("매개변수를 사용한 run() 실패", e);
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
            System.out.println("DB 연결 종료");
        } catch (SQLException e) {
            throw new RuntimeException("DB 종료 실패", e);
        }
    }

    public void startTransaction() {
        try {
            Connection conn = getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("트랜잭션 시작 실패", e);
        }
    }

    public void rollback() {
        try {
            Connection conn = threadLocalConnection.get();
            if (conn != null && !conn.isClosed()) {
                conn.rollback();
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("롤백 실패", e);
        }
    }

//    public void commit() {
//        try {
//            Connection conn = threadLocalConnection.get();
//            if (conn != null && !conn.isClosed()) {
//                if (conn.getAutoCommit()) {
//                    System.out.println("트랜잭션이 시작되지 않았습니다.");
//                    return;
//                }
//                conn.commit();
//                conn.setAutoCommit(true);
//                System.out.println("트랜잭션 커밋 성공");
//            }
//        } catch (SQLException e) {
//            throw new RuntimeException("커밋 실패", e);
//        }
//    }
}