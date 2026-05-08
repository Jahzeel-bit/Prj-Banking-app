import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.swing.JOptionPane;

public class DbHelper {

    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";       // default XAMPP user
    private static final String PASSWORD = "";       // default XAMPP password is empty

    // Method to get a connection
    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println(" Connected to MySQL successfully!");
            return conn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error: " + ex.getMessage());
            System.err.println("❌ Database connection failed: " + ex.getMessage());
            return null;
        }
    }
}