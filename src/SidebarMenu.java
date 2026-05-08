import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * SidebarMenu component for Banking App
 * - Includes Home, Account Details, Transactions, Change Password, Logout
 * - Hover effects + active item highlighting
 */
public class SidebarMenu extends JPanel {

    private final JPanel homeItem;
    private final JPanel accountItem;
    private final JPanel transactionsItem;
    private final JPanel changePasswordItem;
    private final JPanel logoutItem;

    public SidebarMenu(Runnable homeAction, Runnable accountAction, Runnable transactionsAction,
                       Runnable changePasswordAction, Runnable logoutAction) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(0, 82, 164));
        setPreferredSize(new Dimension(200, 0));
        setVisible(true);

        // Create items
        homeItem = createSidebarItem("Home", "src/icons/home.png", homeAction);
        accountItem = createSidebarItem("Account Details", "src/icons/account.png", accountAction);
        transactionsItem = createSidebarItem("Transactions", "src/icons/transactions.png", transactionsAction);
        changePasswordItem = createSidebarItem("Change Password", "src/icons/reset-password.png", changePasswordAction);
        logoutItem = createSidebarItem("Logout", "src/icons/logout.png", logoutAction);

        // Add items with spacing
        add(homeItem);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(accountItem);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(transactionsItem);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(changePasswordItem);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(logoutItem);
    }

    /**
     * Creates a sidebar item with icon + label, hover effect, and click action
     */
    private JPanel createSidebarItem(String text, String iconPath, Runnable action) {
        JPanel itemPanel = new JPanel();
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setBackground(new Color(0, 82, 164));
        itemPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Icon
        ImageIcon icon = new ImageIcon(iconPath);
        Image scaledImage = icon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        JLabel iconLabel = new JLabel(new ImageIcon(scaledImage));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        textLabel.setForeground(Color.WHITE);
        textLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        itemPanel.add(iconLabel);
        itemPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        itemPanel.add(textLabel);

        // Hover + click effects
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!itemPanel.getBackground().equals(Color.GRAY)) {
                    itemPanel.setBackground(new Color(0, 122, 224));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!itemPanel.getBackground().equals(Color.GRAY)) {
                    itemPanel.setBackground(new Color(0, 82, 164));
                }
            }
            public void mouseClicked(MouseEvent e) {
                action.run();
            }
        });

        return itemPanel;
    }

    /**
     * Highlights the active sidebar item in gray
     */
    public void setActiveItem(String itemName) {
        resetItemColors();

        JPanel activePanel = switch (itemName) {
            case "Home" -> homeItem;
            case "Account Details" -> accountItem;
            case "Transactions" -> transactionsItem;
            case "Change Password" -> changePasswordItem;
            case "Logout" -> logoutItem;
            default -> null;
        };

        if (activePanel != null) {
            activePanel.setBackground(Color.GRAY);
        }
    }
    /**
     * Reset all items to default blue color
     */
    private void resetItemColors() {
        homeItem.setBackground(new Color(0, 82, 164));
        accountItem.setBackground(new Color(0, 82, 164));
        transactionsItem.setBackground(new Color(0, 82, 164));
        changePasswordItem.setBackground(new Color(0, 82, 164));
        logoutItem.setBackground(new Color(0, 82, 164));
    }
}
