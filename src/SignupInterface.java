import java.awt.*;
import java.awt.event.MouseAdapter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.swing.*;

class SignupInterface extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField pinField;
    private final JPasswordField passwordField;
    private final JTextField emailField;
    private final JTextField numberField;

    public SignupInterface() {
        setTitle("Sign Up");
        setSize(500, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(245, 245, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 14);

        // Back to Login
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel backLabel = new JLabel("← Back to Login");
        backLabel.setForeground(new Color(0, 102, 204));
        backLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.add(backLabel, gbc);

        // Username
        gbc.gridwidth = 1; gbc.gridx = 0; gbc.gridy = 1;
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(labelFont);
        panel.add(userLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 2;
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        panel.add(emailLabel, gbc);

        gbc.gridx = 1;
        emailField = new JTextField(20);
        panel.add(emailField, gbc);

        // PIN
        gbc.gridx = 0; gbc.gridy = 3;
        JLabel pinLabel = new JLabel("Enter PIN:");
        pinLabel.setFont(labelFont);
        panel.add(pinLabel, gbc);

        gbc.gridx = 1;
        pinField = new JPasswordField(4);
        panel.add(pinField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 4;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(labelFont);
        panel.add(passLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Mobile Number
        gbc.gridx = 0; gbc.gridy = 5;
        JLabel numberLabel = new JLabel("Mobile Number:");
        numberLabel.setFont(labelFont);
        panel.add(numberLabel, gbc);

        gbc.gridx = 1;
        numberField = new JTextField(20);
        panel.add(numberField, gbc);

        // Signup Button
        gbc.gridx = 1; gbc.gridy = 6;
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(new Color(0, 102, 204));
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        signupButton.setFocusPainted(false);
        signupButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        panel.add(signupButton, gbc);

        add(panel);

        // Action
        signupButton.addActionListener(_ -> {
            String username = usernameField.getText().trim();
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String pin = new String(pinField.getPassword()).trim();
            String mobile = numberField.getText().trim();

            // Validate PIN
            int pinInt;
            try {
                pinInt = Integer.parseInt(pin);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "PIN must be numeric.");
                return;
            }

            // Validate Philippine mobile format
            if (!mobile.matches("^09\\d{9}$")) {
                JOptionPane.showMessageDialog(this, "Mobile number must be 11 digits and start with 09.");
                return;
            }

            UserDao dao = new UserDao();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || mobile.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required.");
            } else if (dao.userExist(username)) {
                JOptionPane.showMessageDialog(this, "Username already in use.");
            } else if (dao.emailExist(email)) {
                JOptionPane.showMessageDialog(this, "Email already in use.");
            } else {
                verifyRegistration(username, email, password, mobile, pinInt);
                ImageIcon originalIcon = new ImageIcon("src/icons/check.png");
                Image scaledImage = originalIcon.getImage().getScaledInstance(48, 48, Image.SCALE_SMOOTH);
                ImageIcon resizedIcon = new ImageIcon(scaledImage);

                JOptionPane.showMessageDialog(
                        null,
                        "<html><div style='font-family:Segoe UI; font-size:13px;'>"
                                + "<b style='color:green;'>Registration Complete</b><br>"
                                + "<p style='font-size:12px;'>Account Created!<br>"
                                + "Please Login with your new account.</p>"
                                + "</div></html>",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE,
                        resizedIcon
                );
                dispose();
                new LoginInterface().setVisible(true);
            }
        });

        // Back action
        backLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new LoginInterface().setVisible(true);
            }
        });

        // Filtering PIN to digits only
        pinField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
            }
        });

        // Filtering mobile number to digits only
        numberField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) e.consume();
            }
        });
    }

    // Registration method
    private void verifyRegistration(String username, String email, String password, String number, int pin) {
        try (Connection conn = DbHelper.getConnection()) {
            String sql = "INSERT INTO users (username, email, pin, password, number, balance) VALUES (?, ?, ?, ?, ?, ?)";
            assert conn != null;
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setInt(3, pin);
            stmt.setString(4, password);
            stmt.setString(5, number); // ✅ mobile stored as String
            stmt.setDouble(6, 0);

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                JOptionPane.showMessageDialog(this, "Database error: no rows inserted.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }

    static void main() {
        SwingUtilities.invokeLater(() -> new SignupInterface().setVisible(true));
    }
}
