import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PinValidation extends JFrame {
    private String username;
    private int userId;
    private boolean pinVerified = false;   // flag to check if PIN is correct
    private int attempts = 0;              // counter for attempts

    public PinValidation(String username, int userId) {
        this.userId = userId;
        this.username = username;

        setTitle("Login Verification");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel titleLabel = new JLabel("First, verify your account PIN");
        titleLabel.setFont(new Font("Segue UI", Font.BOLD, 16));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel userLabel = new JLabel(username);
        userLabel.setFont(new Font("Segue UI", Font.PLAIN, 14));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(userLabel);

        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPasswordField pinField = new JPasswordField(20);
        pinField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        pinField.setBorder(new RoundedBorder(15));
        pinField.setMargin(new Insets(5, 10, 5, 10));
        pinField.setForeground(Color.GRAY);
        panel.add(pinField);

        panel.add(Box.createRigidArea(new Dimension(0, 20)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        JButton okButton = new JButton("OK");
        okButton.setBackground(new Color(0, 102, 204));
        okButton.setForeground(Color.WHITE);
        okButton.setFocusPainted(false);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(204, 0, 0));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        panel.add(buttonPanel);
        add(panel);

        // Actions
        okButton.addActionListener(_ -> {
            String pinSt = new String(pinField.getPassword());
            if (pinSt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter your PIN.");
                return;
            }

            attempts++;
            try {
                int pin = Integer.parseInt(pinSt);
                if (validPin(username, pin)) {
                    pinVerified = true;
                    JOptionPane.showMessageDialog(this, "PIN verified!");
                    dispose();
                } else {
                    pinVerified = false;
                    JOptionPane.showMessageDialog(this, "Invalid PIN. Attempt " + attempts + " of 5.");
                    if (attempts >= 5) {
                        JOptionPane.showMessageDialog(this, "Too many failed attempts. Transfer canceled.");
                        dispose();
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "PIN must be numeric.");
            }
        });

        cancelButton.addActionListener(_ -> {
            pinVerified = false;
            dispose();
        });
    }

    public boolean isPinVerified() {
        return pinVerified;
    }

    private boolean validPin(String inputUsername, int pin) {
        String sql = "SELECT * FROM users WHERE username = ? AND pin = ?";
        try (Connection conn = DbHelper.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, inputUsername);
                stmt.setInt(2, pin);

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        this.userId = rs.getInt("id");
                        this.username = rs.getString("username");
                        return true;
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
        return false;
    }

    static void main() {
        SwingUtilities.invokeLater(() -> new PinValidation("user", 0).setVisible(true));
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
