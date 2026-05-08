import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class HomeInterface extends JFrame {
    private final SidebarMenu sidebarPanel;
    private final int userId;
    private String username;
    private double balance;
    private Timer idleTimer;
    private JLabel balanceAmountLabel;
    private final JPanel contentPanel; // swappable center panel

    public HomeInterface(String username, int userId) {
        this.userId = userId;
        this.username = username;

        fetchUserData();

        setTitle("Banking App - Home");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        Image homeIcon = Toolkit.getDefaultToolkit().getImage("src/icons/home.png");
        setIconImage(homeIcon);
        // Start idle timer (5 minutes = 300000 ms)
        startIdleTimer();

        JPanel rootPanel = new JPanel(new BorderLayout());

        // SidebarMenu integration
        sidebarPanel = new SidebarMenu(
                this::showHome,
                this::showAccountDetails,
                this::showTransactionHistory,
                this::showChangePassword,
                this::logout
        );
        rootPanel.add(sidebarPanel, BorderLayout.WEST);

        // Header bar
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(0, 82, 164));

        JButton menuButton = new JButton("= Menu");
        menuButton.setFont(new Font("Segue UI", Font.BOLD, 14));
        menuButton.setForeground(Color.WHITE);
        menuButton.setBackground(new Color(0, 102, 204));
        menuButton.setFocusPainted(false);
        menuButton.addActionListener(_ -> sidebarPanel.setVisible(!sidebarPanel.isVisible()));

        headerPanel.add(menuButton);
        rootPanel.add(headerPanel, BorderLayout.NORTH);

        // Content panel (CardLayout)
        contentPanel = new JPanel(new CardLayout());
        contentPanel.add(createHomeScreen(), "HOME");
        contentPanel.add(new TransactionHistory(userId), "TRANSACTIONS");
        contentPanel.add(createAccountDetailsPanel(), "ACCOUNT");
        contentPanel.add(createChangePasswordPanel(), "CHANGE_PASSWORD");

        rootPanel.add(contentPanel, BorderLayout.CENTER);
        add(rootPanel);
    }

    private JPanel createHomeScreen() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel greetingLabel = new JLabel("Hello, " + username + "!");
        greetingLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(greetingLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // Balance card
        JPanel balanceCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
            }
        };
        balanceCard.setBackground(new Color(0, 102, 204));
        balanceCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        balanceCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        balanceCard.setLayout(new BoxLayout(balanceCard, BoxLayout.Y_AXIS));

        JLabel balanceTitle = new JLabel("Available Balance");
        balanceTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        balanceTitle.setForeground(Color.WHITE);
        balanceTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel amountPanel = new JPanel();
        amountPanel.setOpaque(false);
        amountPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

        ImageIcon walletIcon = new ImageIcon("src/icons/wallet_white.png");
        Image scaledImage = walletIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));

        balanceAmountLabel = new JLabel("₱" + String.format("%.2f", balance));
        balanceAmountLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        balanceAmountLabel.setForeground(Color.WHITE);

        amountPanel.add(iconLabel);
        amountPanel.add(balanceAmountLabel);

        balanceCard.add(balanceTitle);
        balanceCard.add(Box.createRigidArea(new Dimension(0, 10)));
        balanceCard.add(amountPanel);

        mainPanel.add(balanceCard);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        // Cash In and Send Money buttons
        JPanel optionsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        JButton cashInButton = createOptionButton("Cash In", "src/icons/cashin.png", new Color(0, 153, 76));
        JButton sendMoneyButton = createOptionButton("Send Money", "src/icons/send.png", new Color(0, 102, 204));

        cashInButton.addActionListener(_ -> new CashIn(userId, balance, this).setVisible(true));
        sendMoneyButton.addActionListener(_ -> new TransferInterface(userId, username, balance, this).setVisible(true));

        optionsPanel.add(wrapWithShadow(cashInButton, new Color(0, 153, 76)));
        optionsPanel.add(wrapWithShadow(sendMoneyButton, new Color(0, 102, 204)));
        mainPanel.add(optionsPanel);

        return mainPanel;
    }
    private JPanel createAccountDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Account Details for " + username, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Change Password Screen", SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }
    // --- Method for Home navigation ---
    private void showHome() {
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "HOME");
        sidebarPanel.setActiveItem("Home");
    }
    // --- Method for Account Details navigation ---
    private void showAccountDetails() {
        AccountDetails accountDetails = new AccountDetails(userId);
        contentPanel.add(accountDetails, "ACCOUNT");
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "ACCOUNT");
        sidebarPanel.setActiveItem("Account Details");
    }
    // --- Method for Transaction navigation ---
    private void showTransactionHistory() {
        TransactionHistory historyPanel = new TransactionHistory(userId);
        contentPanel.add(historyPanel, "TRANSACTIONS");
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "TRANSACTIONS");
        sidebarPanel.setActiveItem("Transactions");
    }
    // --- Method for Change Password navigation ---
    private void showChangePassword() {
        // Pass a callback that switches back to Home after success
        ChangePassword resetPassPanel = new ChangePassword(userId, () -> {
            CardLayout cl = (CardLayout) contentPanel.getLayout();
            cl.show(contentPanel, "HOME"); // to go back to Home screen
            sidebarPanel.setActiveItem("Home");
        });
        contentPanel.add(resetPassPanel, "CHANGE_PASSWORD");
        CardLayout cl = (CardLayout) contentPanel.getLayout();
        cl.show(contentPanel, "CHANGE_PASSWORD");
        sidebarPanel.setActiveItem("Change Password");
    }

    private void logout() {
        sidebarPanel.setActiveItem("Logout");
        confirmLogout();
    }

    public void refreshBalance() {
        String sql = "SELECT balance FROM users WHERE id = ?";
        try (Connection conn = DbHelper.getConnection()) {
            assert conn != null;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                    balanceAmountLabel.setText("₱" + String.format("%.2f", balance));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error refreshing balance: " + ex.getMessage());
        }
    }

    private JPanel wrapWithShadow(JComponent component, Color bgColor) {
        JPanel shadowPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 20, 20);
            }
        };
        shadowPanel.setBackground(bgColor);
        shadowPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        shadowPanel.add(component, BorderLayout.CENTER);
        return shadowPanel;
    }

    private JButton createOptionButton(String text, String iconPath, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);

        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImage));
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);

        return button;
    }

    private void fetchUserData() {
        String sql = "SELECT balance, username FROM users WHERE id = ?";
        try (Connection conn = DbHelper.getConnection();
             PreparedStatement stmt = conn != null ? conn.prepareStatement(sql) : null) {
            if (stmt != null) {
                stmt.setInt(1, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    balance = rs.getDouble("balance");
                    username = rs.getString("username");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
        }
    }
    // --- New method: confirmation dialog for logout ---
    private void confirmLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            performLogout();
        }
    }
    // --- Actual logout logic ---
    private void performLogout() {
        dispose();
        new LoginInterface().setVisible(true);
    }
    // --- Idle timer setup ---
    private void startIdleTimer() {
        idleTimer = new Timer(300000, _ -> {
            JOptionPane.showMessageDialog(this,
                    "You have been idle for 5 minutes. Logging out automatically.");
            performLogout();
        });
        idleTimer.setRepeats(false);
        idleTimer.start();

        // Reset timer whenever user interacts with the window
        Toolkit.getDefaultToolkit().addAWTEventListener(_ -> {
            if (idleTimer != null) {
                idleTimer.restart();
            }
        }, AWTEvent.KEY_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }
    static void main() {
        SwingUtilities.invokeLater(() -> new HomeInterface("user", 1).setVisible(true));
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}