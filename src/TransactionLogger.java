import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class TransactionLogger {

    // Updated SQL to include transactionId
    private static final String INSERT_SQL =
            "INSERT INTO transactions (transactionId, userId, recipientId, description, amount, type, dateCreated) " +
                    "VALUES (?, ?, ?, ?, ?, ?, NOW())";

    public static void logTransaction(int userId, Integer recipientId,
                                      String description, double amount, String type,
                                      String transactionId) {
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {

            stmt.setString(1, transactionId);
            stmt.setInt(2, userId);

            if (recipientId != null) {
                stmt.setInt(3, recipientId);
            } else {
                stmt.setNull(3, java.sql.Types.INTEGER);
            }

            stmt.setString(4, description);
            stmt.setDouble(5, amount);

            // Ensure type is either "Sent" or "Received"
            if (!"Sent".equalsIgnoreCase(type) && !"Received".equalsIgnoreCase(type)) {
                type = "Sent"; // default fallback
            }
            stmt.setString(6, type);

            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
