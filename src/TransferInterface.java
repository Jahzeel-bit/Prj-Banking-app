import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.UUID;

public class TransferInterface extends JFrame {
    private final int senderId;
    private double senderBalance;
    private final HomeInterface homeInterface;
    private final String username;

    private final JTextField recipientField;
    private final JTextField mobileField;
    private final JTextField amountField;

    public TransferInterface(int userId, String username, double balance, HomeInterface homeInterface) {
        this.senderId = userId;
        this.senderBalance = balance;
        this.homeInterface = homeInterface;
        this.username = username;

        setTitle("Banking App - Transfer");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel rootPanel = new JPanel(new BorderLayout());

        // Gradient header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 102, 204),
                        getWidth(), getHeight(), new Color(0, 153, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JLabel headerLabel = new JLabel("Send Money");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Wrapper background
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(245, 247, 250));

        // Rounded card with shadow
        RoundedPanel cardPanel = new RoundedPanel(25);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        cardPanel.setBackground(Color.WHITE);

        JLabel senderLabel = new JLabel("@" + username + " (Balance: ₱" + senderBalance + ")");
        senderLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        senderLabel.setForeground(Color.DARK_GRAY);
        senderLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardPanel.add(senderLabel);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        recipientField = createStyledField("Recipient Username:", "Enter Username", cardPanel);
        mobileField = createStyledField("Recipient Mobile Number:", "Enter mobile number", cardPanel);
        amountField = createStyledField("Amount to Send:", "Enter amount", cardPanel);

        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton sendButton = new JButton("Send");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        sendButton.setBackground(new Color(0, 153, 76));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                sendButton.setBackground(new Color(0, 180, 90));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                sendButton.setBackground(new Color(0, 153, 76));
            }
        });
        sendButton.addActionListener(_ -> validateRecipientAndProceed());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        cancelButton.setBackground(new Color(204, 0, 0));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(230, 0, 0));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                cancelButton.setBackground(new Color(204, 0, 0));
            }
        });
        cancelButton.addActionListener(_ -> dispose());

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);
        cardPanel.add(buttonPanel);

        wrapperPanel.add(cardPanel, new GridBagConstraints());
        rootPanel.add(wrapperPanel, BorderLayout.CENTER);
        add(rootPanel);
    }

    private JTextField createStyledField(String labelText, String placeholder, JPanel parent) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(Color.DARK_GRAY);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField field = new JTextField(placeholder);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(new RoundedBorder(15));
        field.setMargin(new Insets(5, 10, 5, 10));
        field.setForeground(Color.GRAY);

        field.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.BLACK);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(Color.GRAY);
                }
            }
        });

        parent.add(label);
        parent.add(Box.createRigidArea(new Dimension(0, 5)));
        parent.add(field);
        parent.add(Box.createRigidArea(new Dimension(0, 15)));

        return field;
    }

    // Step 1: Validate recipient before PIN check
    private void validateRecipientAndProceed() {
        String recipientUsername = recipientField.getText().trim();
        String recipientMobile = mobileField.getText().trim();

        if (recipientUsername.isEmpty() && recipientMobile.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter recipient username or mobile number.");
            return;
        }

        String findRecipientSQL = "SELECT id, username, number FROM users WHERE username = ? AND number = ?";
        try (Connection conn = DbHelper.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(findRecipientSQL)) {
                stmt.setString(1, recipientUsername);
                stmt.setString(2, recipientMobile);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    JOptionPane.showMessageDialog(this, "Recipient not found!");
                    return;
                }

                int recipientId = rs.getInt("id");
                String recipientName = rs.getString("username");
                String recipientNumber = rs.getString("number");

                PinValidation pinValidation = new PinValidation(username, senderId);
                pinValidation.setVisible(true);

                pinValidation.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        if (pinValidation.isPinVerified()) {
                            performTransfer(recipientId, recipientName, recipientNumber);
                        } else {
                            JOptionPane.showMessageDialog(null, "Transfer canceled: PIN verification failed.");
                        }
                    }
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    // Step 2: Perform transfer only if PIN verified
    private void performTransfer(int recipientId, String recipientName, String recipientNumber) {
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount!");
            return;
        }

        if (amount <= 0 || amount > senderBalance) {
            JOptionPane.showMessageDialog(this, "Invalid or insufficient balance!");
            return;
        }

        String updateSenderSQL = "UPDATE users SET balance = balance - ? WHERE id = ?";
        String updateRecipientSQL = "UPDATE users SET balance = balance + ? WHERE id = ?";

        try (Connection conn = DbHelper.getConnection()) {
            assert conn != null;
            try (PreparedStatement updateSenderStmt = conn.prepareStatement(updateSenderSQL);
                 PreparedStatement updateRecipientStmt = conn.prepareStatement(updateRecipientSQL)) {

                conn.setAutoCommit(false);

                updateSenderStmt.setDouble(1, amount);
                updateSenderStmt.setInt(2, senderId);
                updateSenderStmt.executeUpdate();

                updateRecipientStmt.setDouble(1, amount);
                updateRecipientStmt.setInt(2, recipientId);
                updateRecipientStmt.executeUpdate();

                conn.commit();

                senderBalance -= amount;

                // Generate transactionId
                String transactionId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                // Log transactions with transactionId
                TransactionLogger.logTransaction(senderId, recipientId,
                        "₱" + amount + " to " + recipientName + " (" + recipientNumber + ")",
                        amount, "Sent", transactionId);

                TransactionLogger.logTransaction(recipientId, senderId,
                        "₱" + amount + " from " + username,
                        amount, "Received", transactionId);

                showSuccessDialog(transactionId, recipientName, recipientNumber, amount);

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

    private void showSuccessDialog(String transactionId, String recipientName, String recipientNumber, double amount) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        ImageIcon checkIcon = new ImageIcon("src/icons/check.png");
        Image scaledImage = checkIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel message = new JLabel("<html><b>Transaction Completed!</b><br>"
                + "Transaction #: " + transactionId + "<br>"
                + "Recipient: " + recipientName + "<br>"
                + "Mobile: " + recipientNumber + "<br>"
                + "Amount: ₱" + String.format("%.2f", amount) + "</html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(message, BorderLayout.CENTER);

        // Show dialog with OK button
        int option = JOptionPane.showOptionDialog(
                this,
                panel,
                "Success",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                new Object[]{"OK"},
                "OK"
        );
        if (option == 0) {
            // Refresh balance in HomeInterface
            if (homeInterface != null) {
                homeInterface.refreshBalance();
            }
            // Close TransferInterface
            dispose();
        }
    }
}
