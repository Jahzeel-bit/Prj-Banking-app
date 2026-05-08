import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ChangePassword extends JPanel {
    private final int userId;
    private final Runnable onSuccessReturnHome; // callback to go back to Home

    public ChangePassword(int userId, Runnable onSuccessReturnHome) {
        this.userId = userId;
        this.onSuccessReturnHome = onSuccessReturnHome;
        setLayout(new BorderLayout());

        // Header bar with gradient style
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
        JLabel headerLabel = new JLabel("Change Password");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Wrapper to center card
        JPanel wrapperPanel = new JPanel(new GridBagLayout());
        wrapperPanel.setBackground(new Color(245, 247, 250)); // soft background

        // Rounded card with shadow
        RoundedPanel cardPanel = new RoundedPanel(25);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        cardPanel.setBackground(Color.WHITE);

        // Old password
        cardPanel.add(createInfoRow("src/icons/lock.png", "Enter Current Password:"));
        JPasswordField oldPasswordField = createPasswordField();
        cardPanel.add(oldPasswordField);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // New password + strength indicator
        cardPanel.add(createInfoRow("src/icons/key.png", "Enter New Password:"));
        JPasswordField newPasswordField = createPasswordField();
        cardPanel.add(newPasswordField);

        JProgressBar strengthBar = new JProgressBar(0, 100);
        strengthBar.setPreferredSize(new Dimension(250, 15));
        strengthBar.setForeground(new Color(0, 153, 76));
        strengthBar.setBackground(new Color(220, 220, 220));
        strengthBar.setStringPainted(true);
        strengthBar.setString("Strength: Weak");
        cardPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        cardPanel.add(strengthBar);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        // Update strength indicator dynamically
        newPasswordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void updateStrength() {
                String pass = new String(newPasswordField.getPassword());
                int score = calculateStrength(pass);
                strengthBar.setValue(score);
                if (score < 40) {
                    strengthBar.setString("Strength: Weak");
                    strengthBar.setForeground(Color.RED);
                } else if (score < 70) {
                    strengthBar.setString("Strength: Medium");
                    strengthBar.setForeground(new Color(255, 153, 0));
                } else {
                    strengthBar.setString("Strength: Strong");
                    strengthBar.setForeground(new Color(0, 153, 76));
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateStrength(); }
        });

        // Confirm new password
        cardPanel.add(createInfoRow("src/icons/key.png", "Re-enter New Password:"));
        JPasswordField confirmPasswordField = createPasswordField();
        cardPanel.add(confirmPasswordField);
        cardPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Change button with hover effect
        JButton changeButton = new JButton("Update Password");
        changeButton.setBackground(new Color(0, 102, 204));
        changeButton.setForeground(Color.WHITE);
        changeButton.setFont(new Font("Segoe UI", Font.BOLD, 18));
        changeButton.setFocusPainted(false);
        changeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        changeButton.setPreferredSize(new Dimension(200, 45));

        changeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                changeButton.setBackground(new Color(0, 120, 230));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                changeButton.setBackground(new Color(0, 102, 204));
            }
        });

        changeButton.addActionListener(_ -> {
            String oldPass = new String(oldPasswordField.getPassword());
            String newPass = new String(newPasswordField.getPassword());
            String confirmPass = new String(confirmPasswordField.getPassword());

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
                return;
            }
            if (!newPass.equals(confirmPass)) {
                JOptionPane.showMessageDialog(this, "New passwords do not match.");
                return;
            }

            if (updatePassword(oldPass, newPass)) {
                showSuccessDialog();
                // Clear fields
                oldPasswordField.setText("");
                newPasswordField.setText("");
                confirmPasswordField.setText("");
                strengthBar.setValue(0);
                strengthBar.setString("Strength: Weak");
                strengthBar.setForeground(Color.RED);
                // Return to Home screen
                if (onSuccessReturnHome != null) {
                    onSuccessReturnHome.run();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Current password incorrect.");
            }
        });

        cardPanel.add(changeButton);

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

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField(20);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private int calculateStrength(String pass) {
        int score = 0;
        if (pass.length() >= 6) score += 30;
        if (pass.matches(".*[A-Z].*")) score += 20;
        if (pass.matches(".*[0-9].*")) score += 20;
        if (pass.matches(".*[!@#$%^&*()].*")) score += 30;
        return Math.min(score, 100);
    }

    private boolean updatePassword(String oldPass, String newPass) {
        String sqlCheck = "SELECT password FROM users WHERE id = ?";
        String sqlUpdate = "UPDATE users SET password = ? WHERE id = ?";

        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmtCheck = conn != null ? conn.prepareStatement(sqlCheck) : null;
             PreparedStatement stmtUpdate = conn != null ? conn.prepareStatement(sqlUpdate) : null) {

            if (stmtCheck != null && stmtUpdate != null) {
                stmtCheck.setInt(1, userId);
                ResultSet rs = stmtCheck.executeQuery();
                if (rs.next()) {
                    String currentPass = rs.getString("password");
                    if (!currentPass.equals(oldPass)) {
                        return false;
                    }
                }
                stmtUpdate.setString(1, newPass);
                stmtUpdate.setInt(2, userId);
                stmtUpdate.executeUpdate();
                return true;
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error updating password: " + ex.getMessage());
        }
        return false;
    }
    private void showSuccessDialog() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        ImageIcon checkIcon = new ImageIcon("src/icons/check.png");
        Image scaledImage = checkIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        panel.add(iconLabel, BorderLayout.WEST);

        JLabel message = new JLabel("<html><b>Password Updated Successfully!</b><br>"
                + "Your password has been changed.</html>");
        message.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(message, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(this, panel, "Success", JOptionPane.PLAIN_MESSAGE);
    }
}
