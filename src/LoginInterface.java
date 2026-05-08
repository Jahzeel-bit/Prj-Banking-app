import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;

class LoginInterface extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private String username;
    private String password;
    private int userId;
    public LoginInterface(){
        //JFrame
        setTitle("Online Banking App");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        Image loginIcon = Toolkit.getDefaultToolkit().getImage("src/icons/login.png");
        setIconImage(loginIcon);

        //  GridBagLayout
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("Segue UI", Font.BOLD, 14);

        //  set icon
        ImageIcon originalIcon = new ImageIcon("src/icons/login.png");
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(scaledImage);

        // add resizedIcon to JLabel iconInterface
        JLabel iconInterface = new JLabel(resizedIcon);
        iconInterface.setHorizontalAlignment(JLabel.CENTER);

        // Position at top center
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(iconInterface, gbc);

        // Username Label
        gbc.gridwidth = 1;
        gbc.gridx = 0; // column 0
        gbc.gridy = 1; // row 1
        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(labelFont);
        panel.add(userLabel, gbc);

        // Username Field
        gbc.gridx = 1; // column 1
        gbc.gridy = 1;
        usernameField = new JTextField(20);
        panel.add(usernameField, gbc);

        // Password Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(labelFont);
        panel.add(passLabel, gbc);

        // Password Field
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);
        panel.add(passwordField, gbc);

        // Login Button
        gbc.gridx = 1;
        gbc.gridy = 3;
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 102, 204)); // green
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Segue UI", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        panel.add(loginButton, gbc);

        // Signup Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        JButton signupButton = new JButton("Sign Up");
        signupButton.setBackground(new Color(0, 102, 204)); // green
        signupButton.setForeground(Color.WHITE);
        signupButton.setFont(new Font("Segue UI", Font.BOLD, 14));
        signupButton.setFocusPainted(true);
        signupButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        panel.add(signupButton, gbc);

        // Add panel to frame
        add(panel);

        // Button action
        loginButton.addActionListener(_ -> {
            String inputUsername = getUsername();
            String inputPassword = getPassword();

            if(inputUsername.isEmpty() || inputPassword.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Invalid credentials!");
            } else if(validateLogin(inputUsername,inputPassword)){
                JOptionPane.showMessageDialog(null, "Login Successful");
                dispose();
                new HomeInterface(username, userId).setVisible(true);
            }else{
                JOptionPane.showMessageDialog(null, "Invalid credentials!");
            }
        });
        signupButton.addActionListener(_ -> {
            dispose(); // close signup window
            new SignupInterface().setVisible(true); // open login window
        });
    }
    private boolean validateLogin(String inputUsername, String inputPassword) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            try (Connection conn = DbHelper.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, inputUsername);
                stmt.setString(2, inputPassword); 

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        // ✅ User found with matching credentials
                        this.userId = rs.getInt("id");
                        this.username = rs.getString("username");
                        return true;
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            }
            return false; //  No match found
    }
       
    
    //getters
    public String getUsername() {
        return usernameField.getText();
    }
    public String getPassword(){
        return new String(passwordField.getPassword());
    }
    //setters
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    static void main() {
        SwingUtilities.invokeLater(() -> new LoginInterface().setVisible(true));
    }
}