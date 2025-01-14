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
    private Connection conn;

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
        String url = DB_PROTOCOL + host + "/" + project;
        conn = DriverManager.getConnection(url, username, password);

        return conn;
    }

    public void run(String query) {
        try (
                var stmt = getConnection().createStatement()
        ) {
            stmt.execute(query);
        } catch (SQLException e) {
            System.out.println("run() 실행 실패");
            e.printStackTrace();
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
            System.out.println("매개변수를 사용한 run() 실패");
            e.printStackTrace();
        }
    }

    public Sql genSql() {
        return new Sql(this);
    }

    public void close() {
        try {
            conn.close();
            System.out.println("DB 연결 종료");
        } catch (SQLException e) {
            System.out.println("close() 실행 실패");
            e.printStackTrace();
        }
    }
}
