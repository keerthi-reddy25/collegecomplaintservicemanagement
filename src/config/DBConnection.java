package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to the MySQL database.
 *
 * BEFORE RUNNING: replace YOUR_MYSQL_PASSWORD with your own MySQL root password
 * (and change USERNAME if you do not use "root").
 */
public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/college_complaint_db?allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "keerthi@6677";

    // Utility class: no objects should be created from it.
    private DBConnection() {
    }

    /**
     * Opens a new connection. The caller must close it
     * (use try-with-resources).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Tries to connect once and prints a helpful message if it fails.
     * Used by Main when the program starts.
     *
     * @return true if the connection works
     */
    public static boolean testConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("ERROR: MySQL Connector/J JAR was not found.");
            System.out.println("Add mysql-connector-j-x.x.x.jar to the Eclipse Build Path (see README.md).");
            return false;
        }

        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            System.out.println("ERROR: Could not connect to the database.");
            System.out.println("Reason: " + e.getMessage());
            System.out.println("Check that:");
            System.out.println("  1. MySQL server is running.");
            System.out.println("  2. You ran database/schema.sql.");
            System.out.println("  3. USERNAME and PASSWORD in DBConnection.java are correct.");
            return false;
        }
    }
}
