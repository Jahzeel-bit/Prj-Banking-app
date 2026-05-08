import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AccountDetails extends JPanel {
    private final int userId;
    private String username;
    private String email;
    private String mobile;
    private double balance;

    public AccountDetails(int userId) {
        this.userId = userId;
        setLayout(new BorderLayout());

        // Gradient header bar
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(0, 102, 204),
                        getWidth(), getHeight(), new Color(0, 153, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        JLabel headerLabel = new JLabel("Account Details");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Wrapper background
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(245, 247, 250)); // soft gray

        // Rounded card with shadow
        RoundedPanel cardPanel = new RoundedPanel(25);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        cardPanel.setBackground(Color.WHITE);

        fetchAccountData();

        // Account info rows
        cardPanel.add(createInfoRow("src/icons/user.png", "Username: " + username));
        cardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        cardPanel.add(createInfoRow("src/icons/email.png", "Email: " + email));
        cardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        cardPanel.add(createInfoRow("src/icons/phone.png", "Mobile: " + mobile));
        cardPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        cardPanel.add(createInfoRow("src/icons/money.png", "Balance: ₱" + String.format("%.2f", balance)));
        cardPanel.add(Box.createRigidArea(new Dimension(0, 25)));

        // Change PIN button with hover effect
        JButton changePinButton = new JButton("Change PIN");
        changePinButton.setBackground(new Color(0, 102, 204));
        changePinButton.setForeground(Color.WHITE);
        changePinButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        changePinButton.setFocusPainted(false);
        changePinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changePinButton.setPreferredSize(new Dimension(180, 45));

        changePinButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                changePinButton.setBackground(new Color(0, 120, 230));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                changePinButton.setBackground(new Color(0, 102, 204));
            }
        });

        changePinButton.addActionListener(_ -> openPinValidation());
        cardPanel.add(changePinButton);

        wrapperPanel.add(cardPanel, new GridBagConstraints());
        add(wrapperPanel, BorderLayout.CENTER);
    }

    private JPanel createInfoRow(String iconPath, String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        row.setOpaque(false);

        ImageIcon icon = new ImageIcon(iconPath);
        Image scaled = icon.getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaled));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        label.setForeground(Color.DARK_GRAY);

        row.add(iconLabel);
        row.add(label);
        return row;
    }

    private void fetchAccountData() {
        String sql = "SELECT username, email, number, balance FROM users WHERE id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(sql) : null) {
            if (stmt != null) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    username = rs.getString("username");
                    email = rs.getString("email");
                    mobile = rs.getString("number");
                    balance = rs.getDouble("balance");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading account details: " + ex.getMessage());
        }
    }

    private void openPinValidation() {
        PinValidation pinValidation = new PinValidation(username, userId);
        pinValidation.setVisible(true);

        pinValidation.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (pinValidation.isPinVerified()) {
                    promptNewPin();
                } else {
                    JOptionPane.showMessageDialog(null, "PIN verification failed.");
                }
            }
        });
    }

    private void promptNewPin() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setBackground(Color.WHITE);

        JLabel newPinLabel = new JLabel("Enter New PIN:");
        JPasswordField newPinField = new JPasswordField(4);

        JLabel rePinLabel = new JLabel("Re-enter New PIN:");
        JPasswordField rePinField = new JPasswordField(4);

        // Digit-only filtering
        java.awt.event.KeyAdapter digitFilter = new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
            }
        };
        newPinField.addKeyListener(digitFilter);
        rePinField.addKeyListener(digitFilter);

        panel.add(newPinLabel);
        panel.add(newPinField);
        panel.add(rePinLabel);
        panel.add(rePinField);

        int option = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Change PIN",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            String newPin = new String(newPinField.getPassword());
            String rePin = new String(rePinField.getPassword());

            if (newPin.length() != 4) {
                JOptionPane.showMessageDialog(null, "PIN must be exactly 4 digits.");
                return;
            }

            if (!newPin.equals(rePin)) {
                JOptionPane.showMessageDialog(null, "PINs do not match.");
                return;
            }

            try {
                int pinInt = Integer.parseInt(newPin);
                updatePin(pinInt);
                JOptionPane.showMessageDialog(null, "PIN successfully updated!");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "PIN must be numeric.");
            }
        }
    }

    private void updatePin(int newPin) {
        String sql = "UPDATE users SET pin = ? WHERE id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(sql) : null) {
            if (stmt != null) {
                stmt.setInt(1, newPin);
                stmt.setInt(2, userId);
                stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating PIN: " + ex.getMessage());
        }
    }
}
