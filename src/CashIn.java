import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class CashIn extends JFrame {
    private final int userId;
    private final HomeInterface homeInterface;
    private double currentBalance;

    private final JTextField amountField;

    public CashIn(int userId, double balance, HomeInterface homeInterface) {
        this.userId = userId;
        this.currentBalance = balance;
        this.homeInterface = homeInterface;

        setTitle("Banking App - Cash In");
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());

        // Header bar styled like HomeInterface
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(0, 102, 204));
        JLabel headerLabel = new JLabel("Cash In");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Main content with card style
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Balance card
        JPanel balanceCard = new JPanel();
        balanceCard.setBackground(new Color(0, 102, 204));
        balanceCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));
        balanceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

        JLabel balanceTitle = new JLabel("Current Balance");
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        balanceTitle.setForeground(Color.WHITE);
        balanceTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel balanceLabel = new JLabel("₱" + String.format("%.2f", currentBalance));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        balanceCard.add(balanceTitle);
        balanceCard.add(Box.createRigidArea(new Dimension(0, 8)));
        balanceCard.add(balanceLabel);

        mainPanel.add(balanceCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Amount field
        JLabel amountLabel = new JLabel("Enter Amount to Deposit:");
        amountLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        mainPanel.add(amountLabel);

        amountField = new JTextField();
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        amountField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        mainPanel.add(amountField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton depositButton = new JButton("Deposit");
        depositButton.setBackground(new Color(0, 153, 76));
        depositButton.setForeground(Color.WHITE);
        depositButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        depositButton.setFocusPainted(false);
        depositButton.addActionListener(_ -> performCashIn());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(204, 0, 0));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(_ -> dispose());

        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel);

        rootPanel.add(mainPanel, BorderLayout.CENTER);
        add(rootPanel);
    }

    private void performCashIn() {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
            return;
        }

        if (amount <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be greater than zero.");
            return;
        }

        String updateSQL = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DbHelper.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(updateSQL)) {
                stmt.setDouble(1, amount);
                stmt.setInt(2, userId);
                stmt.executeUpdate();

                currentBalance += amount;

                String transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                TransactionLogger.logTransaction(userId, null,
                        "Cash In ₱" + amount,
                        amount, "Received", transactionId);

                showSuccessDialog(transactionId, amount);

                if (homeInterface != null) {
                    homeInterface.refreshBalance();
                }

                dispose();

            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    private void showSuccessDialog(String transactionId, double amount) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        ImageIcon checkIcon = new ImageIcon("src/icons/check.png");
        Image scaledImage = checkIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel message = new JLabel("<html><b>Cash In Successful!</b><br>"
                + "Transaction #: " + transactionId + "<br>"
                + "Amount: ₱" + String.format("%.2f", amount) + "</html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(message, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Success", JOptionPane.PLAIN_MESSAGE);
    }
}
