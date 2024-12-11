// LeftPanel.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LeftPanel extends JPanel {
    public static JButton loginButton;
    public static JButton adminLoginButton;
    private Image backgroundImage = new ImageIcon("res/left-bg.png").getImage();
    public LeftPanel() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginLabel = new JLabel("Already have an account?");
        loginButton = new JButton("Login");
        adminLoginButton = new JButton("Administration");

        loginLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 30));
        loginLabel.setForeground(new Color(9, 9, 56));
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setBackground(Color.BLUE);
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 50));
        adminLoginButton.setFont(new Font("Arial", Font.BOLD, 14));
        adminLoginButton.setBackground(Color.RED);
        adminLoginButton.setForeground(Color.WHITE);
        adminLoginButton.setPreferredSize(new Dimension(200, 50));

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(loginLabel, gbc);
        gbc.gridy = 1;
        add(loginButton, gbc);
        gbc.gridy = 2;
        add(adminLoginButton, gbc);
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)); // Set opacity to 50%
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        }
    }
}