import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TransactionHistory screen
 * - Displays recent transactions with professional styling
 * - Each transaction shows ID, description, amount, type, and timestamp
 * - Red with "-" for deductions, green with "+" for credits
 */
public class TransactionHistory extends JPanel {

    private final int userId;

    public TransactionHistory(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // Header bar
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(0, 102, 204));
        JLabel headerLabel = new JLabel("Transaction History");
        headerLabel.setFont(new Font("Segue UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Scrollable transaction list
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        List<Transaction> transactions = fetchTransactions();

        if (transactions.isEmpty()) {
            JLabel emptyLabel = new JLabel("No transactions found.");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            emptyLabel.setForeground(Color.GRAY);
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            listPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            listPanel.add(emptyLabel);
        } else {
            for (Transaction tx : transactions) {
                listPanel.add(createTransactionItem(tx));
                listPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Fetch transactions from DB for this user
     */
    private List<Transaction> fetchTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT transactionId, description, amount, type, dateCreated FROM transactions WHERE userId = ? ORDER BY dateCreated DESC";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(sql) : null) {
            assert stmt != null;
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("transactionId");
                String desc = rs.getString("description");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                String date = rs.getString("dateCreated");
                list.add(new Transaction(id, desc, amount, type, date));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + ex.getMessage());
        }
        return list;
    }

    /**
     * Create a styled transaction item
     */
    private JPanel createTransactionItem(Transaction tx) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        // Transaction ID
        JLabel idLabel = new JLabel("ID: " + tx.transactionId);
        idLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        idLabel.setForeground(Color.GRAY);

        // Description
        JLabel descLabel = new JLabel(tx.description);
        descLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        descLabel.setForeground(Color.BLACK);

        // Date
        JLabel dateLabel = new JLabel(tx.date);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);

        // Amount with +/- and color
        String amountText;
        Color amountColor;
        if (tx.type.equalsIgnoreCase("DEBIT") || tx.type.equalsIgnoreCase("Sent")) {
            amountText = "- ₱" + String.format("%.2f", tx.amount);
            amountColor = Color.RED;
        } else {
            amountText = "+ ₱" + String.format("%.2f", tx.amount);
            amountColor = new Color(0, 153, 76);
        }

        JLabel amountLabel = new JLabel(amountText);
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        amountLabel.setForeground(amountColor);

        // Left panel (ID, description, date)
        JPanel leftPanel = new JPanel();
        leftPanel.setOpaque(false);
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(idLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(descLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        leftPanel.add(dateLabel);

        itemPanel.add(leftPanel, BorderLayout.WEST);
        itemPanel.add(amountLabel, BorderLayout.EAST);

        return itemPanel;
    }

    /**
     * Transaction model
     */
    private static class Transaction {
        String transactionId;
        String description;
        double amount;
        String type;
        String date;

        Transaction(String transactionId, String description, double amount, String type, String date) {
            this.transactionId = transactionId;
            this.description = description;
            this.amount = amount;
            this.type = type;
            this.date = date;
        }
    }
}
