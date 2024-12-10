import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.util.Properties;


public class Main {
    private static Properties loadDatabaseProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("db.properties")) {
            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static Connection getConnection() throws SQLException {
        Properties properties = loadDatabaseProperties();
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private static boolean authenticateAdmin(String username, String password) {
        String query = "SELECT * FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean authenticateUser(String username, String password) {
        try (Connection conn = getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ? AND approved = TRUE";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleRegistration(String fullName, String username, String password, String dob, String presentAddress, String permanentAddress, String sex, String phoneNumber, String image) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO pending_users (full_name, username, password, dob, present_address, permanent_address, sex, phone_number, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            insertStmt.setString(1, fullName);
            insertStmt.setString(2, username);
            insertStmt.setString(3, password);
            insertStmt.setString(4, dob);
            insertStmt.setString(5, presentAddress);
            insertStmt.setString(6, permanentAddress);
            insertStmt.setString(7, sex);
            insertStmt.setString(8, phoneNumber);
            insertStmt.setString(9, image);
            insertStmt.executeUpdate();

            JOptionPane.showMessageDialog(null, "Registration successful", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void loadPendingRegistrations(JPanel pendingPanel) {
        pendingPanel.removeAll();
        try (Connection conn = getConnection()) {
            String query = "SELECT full_name, username, dob, present_address, permanent_address, sex, phone_number, image FROM pending_users";
            PreparedStatement stmt = conn.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String fullName = rs.getString("full_name");
                String username = rs.getString("username");
                String dob = rs.getString("dob");
                String presentAddress = rs.getString("present_address");
                String permanentAddress = rs.getString("permanent_address");
                String sex = rs.getString("sex");
                String phoneNumber = rs.getString("phone_number");
                String image = rs.getString("image");

                JPanel userPanel = new JPanel(new BorderLayout());
                JLabel userLabel = new JLabel("<html>Full Name: " + fullName + "<br>Username: " + username + "<br>Date of Birth: " + dob + "<br>Present Address: " + presentAddress + "<br>Permanent Address: " + permanentAddress + "<br>Sex: " + sex + "<br>Phone Number: " + phoneNumber + "</html>");
                JButton approveButton = new JButton("Approve");
                JLabel imageLabel = new JLabel();
                if (image != null) {
                    imageLabel.setIcon(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
                }

                approveButton.addActionListener(e -> {
                    approveRegistration(username);
                    pendingPanel.remove(userPanel);
                    pendingPanel.revalidate();
                    pendingPanel.repaint();
                });

                userPanel.add(imageLabel, BorderLayout.WEST);
                userPanel.add(userLabel, BorderLayout.CENTER);
                userPanel.add(approveButton, BorderLayout.EAST);
                pendingPanel.add(userPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pendingPanel.revalidate();
        pendingPanel.repaint();
    }

    private static void approveRegistration(String username) {
        try (Connection conn = getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String query = "INSERT INTO users (full_name, username, password, dob, present_address, permanent_address, sex, phone_number, image, approved) SELECT full_name, username, password, dob, present_address, permanent_address, sex, phone_number, image, TRUE FROM pending_users WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.executeUpdate();

            String deleteQuery = "DELETE FROM pending_users WHERE username = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setString(1, username);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void loadUserProfile(String username, JPanel profilePanel) {
        String query = "SELECT full_name, username, dob, present_address, permanent_address, sex, phone_number, image FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                profilePanel.removeAll();
                profilePanel.setLayout(new GridLayout(0, 1));
                profilePanel.add(new JLabel("Full Name: " + rs.getString("full_name")));
                profilePanel.add(new JLabel("Username: " + rs.getString("username")));
                profilePanel.add(new JLabel("Date of Birth: " + rs.getString("dob")));
                profilePanel.add(new JLabel("Present Address: " + rs.getString("present_address")));
                profilePanel.add(new JLabel("Permanent Address: " + rs.getString("permanent_address")));
                profilePanel.add(new JLabel("Sex: " + rs.getString("sex")));
                profilePanel.add(new JLabel("Phone Number: " + rs.getString("phone_number")));
                String image = rs.getString("image");
                if (image != null) {
                    JLabel imageLabel = new JLabel(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
                    profilePanel.add(imageLabel);
                }
                profilePanel.revalidate();
                profilePanel.repaint();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame frame = new JFrame("User Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full-sized window
        frame.setLayout(new BorderLayout());

        // Top section for app name
        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                ImageIcon icon = new ImageIcon("res/top-bg.jpg");
                Image img = icon.getImage();
                g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            }
        };
        topPanel.setPreferredSize(new Dimension(frame.getWidth(), 100));
        topPanel.setLayout(new BorderLayout());

// Add logo
        // Add logo and resize it
        ImageIcon logoIcon = new ImageIcon("res/logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH); // Resize the logo
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(logoLabel, BorderLayout.WEST);

        frame.add(topPanel, BorderLayout.NORTH);

        // Main panel with card layout
        JPanel mainPanel = new JPanel(new CardLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        // Home panel
        JPanel homePanel = new JPanel(new GridLayout(1, 2));
        mainPanel.add(homePanel, "Home");

        // Left section for login and administration
        JPanel leftPanel = new JPanel(new GridBagLayout()) {
            private Image backgroundImage = new ImageIcon("res/left-bg.png").getImage();

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
        };

        leftPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginLabel = new JLabel("Already have an account?");
        JButton loginButton = new JButton("Login");
        JButton adminLoginButton = new JButton("Administration");

        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(loginLabel, gbc);
        gbc.gridy = 1;
        leftPanel.add(loginButton, gbc);
        gbc.gridy = 2;
        leftPanel.add(adminLoginButton, gbc);

        homePanel.add(leftPanel);

        // Right section for registration
        JPanel rightPanel = new JPanel(new GridBagLayout()) {
            private Image backgroundImage = new ImageIcon("res/right-bg.png").getImage();

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (backgroundImage != null) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // Set opacity to 50%
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
                    g2d.dispose();
                }
            }
        };
        rightPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel registrationLabel = new JLabel("Registration");
        JTextField fullNameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        final JFormattedTextField dobField;
        try {
            MaskFormatter dateMask = new MaskFormatter("####-##-##");
            dateMask.setPlaceholderCharacter('_');
            dobField = new JFormattedTextField(dateMask);
        } catch (ParseException e) {
            throw new RuntimeException("Date mask creation failed", e);
        }
        JTextField presentAddressField = new JTextField();
        JTextField permanentAddressField = new JTextField();
        JTextField sexField = new JTextField();
        JTextField phoneNumberField = new JTextField();
        JLabel imageLabel = new JLabel();
        JLabel imageDisplay = new JLabel();
        JButton imageButton = new JButton("Select Image");
        JButton registerButton = new JButton("Signup");

        gbc.gridx = 0;
        gbc.gridy = 0;
        rightPanel.add(registrationLabel, gbc);
        gbc.gridy = 1;
        rightPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        rightPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        rightPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        rightPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(dobField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        rightPanel.add(new JLabel("Present Address:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(presentAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        rightPanel.add(new JLabel("Permanent Address:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(permanentAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        rightPanel.add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(sexField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        rightPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        rightPanel.add(phoneNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        rightPanel.add(registerButton, gbc);
        gbc.gridx = 1;
        rightPanel.add(imageButton, gbc);

        homePanel.add(rightPanel);

        // Admin login panel
        JPanel adminLoginPanel = new JPanel(new GridBagLayout());
        adminLoginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField adminUsernameField = new JTextField();
        JPasswordField adminPasswordField = new JPasswordField();
        JButton adminLoginSubmitButton = new JButton("Login");
        JButton adminBackButton = new JButton("Back");

        gbc.gridx = 0;
        gbc.gridy = 0;
        adminLoginPanel.add(new JLabel("Admin Username:"), gbc);
        gbc.gridx = 1;
        adminLoginPanel.add(adminUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        adminLoginPanel.add(new JLabel("Admin Password:"), gbc);
        gbc.gridx = 1;
        adminLoginPanel.add(adminPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        adminLoginPanel.add(adminLoginSubmitButton, gbc);

        gbc.gridy = 3;
        adminLoginPanel.add(adminBackButton, gbc);

        mainPanel.add(adminLoginPanel, "AdminLogin");

        // User login panel
        JPanel userLoginPanel = new JPanel(new GridBagLayout());
        userLoginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField userUsernameField = new JTextField();
        JPasswordField userPasswordField = new JPasswordField();
        JButton userLoginSubmitButton = new JButton("Login");
        JButton userBackButton = new JButton("Back");

        gbc.gridx = 0;
        gbc.gridy = 0;
        userLoginPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        userLoginPanel.add(userUsernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        userLoginPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        userLoginPanel.add(userPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        userLoginPanel.add(userLoginSubmitButton, gbc);

        gbc.gridy = 3;
        userLoginPanel.add(userBackButton, gbc);

        mainPanel.add(userLoginPanel, "UserLogin");

        // Admin approval panel
        JPanel adminPanel = new JPanel(new BorderLayout());
        JPanel pendingPanel = new JPanel(new GridLayout(0, 1));
        JButton refreshButton = new JButton("Refresh");
        JButton adminApprovalBackButton = new JButton("Back");
        adminPanel.add(new JScrollPane(pendingPanel), BorderLayout.CENTER);
        adminPanel.add(refreshButton, BorderLayout.SOUTH);
        adminPanel.add(adminApprovalBackButton, BorderLayout.NORTH);

        mainPanel.add(adminPanel, "Admin");

        // User profile panel
        JPanel profilePanel = new JPanel();
        JButton profileBackButton = new JButton("Back");
        profilePanel.add(profileBackButton);
        mainPanel.add(profilePanel, "Profile");

        CardLayout cl = (CardLayout) mainPanel.getLayout();

        adminLoginButton.addActionListener(e -> cl.show(mainPanel, "AdminLogin"));

        adminLoginSubmitButton.addActionListener(e -> {
            String username = adminUsernameField.getText();
            String password = new String(adminPasswordField.getPassword());
            if (authenticateAdmin(username, password)) {
                loadPendingRegistrations(pendingPanel);
                cl.show(mainPanel, "Admin");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid admin credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        adminBackButton.addActionListener(e -> cl.show(mainPanel, "Home"));
        adminApprovalBackButton.addActionListener(e -> cl.show(mainPanel, "AdminLogin"));

        loginButton.addActionListener(e -> cl.show(mainPanel, "UserLogin"));

        userLoginSubmitButton.addActionListener(e -> {
            String username = userUsernameField.getText();
            String password = new String(userPasswordField.getPassword());
            if (authenticateUser(username, password)) {
                loadUserProfile(username, profilePanel);
                cl.show(mainPanel, "Profile");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        userBackButton.addActionListener(e -> cl.show(mainPanel, "Home"));
        profileBackButton.addActionListener(e -> cl.show(mainPanel, "UserLogin"));

        registerButton.addActionListener(e -> {
            String fullName = fullNameField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String dob = dobField.getText();
            String presentAddress = presentAddressField.getText();
            String permanentAddress = permanentAddressField.getText();
            String sex = sexField.getText();
            String phoneNumber = phoneNumberField.getText();
            String image = imageLabel.getText();
            handleRegistration(fullName, username, password, dob, presentAddress, permanentAddress, sex, phoneNumber, image);
        });

        imageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedFile.getAbsolutePath()).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT));
                imageDisplay.setIcon(imageIcon);
                imageDisplay.setText(""); // Clear any previous text
                imageLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        refreshButton.addActionListener(e -> loadPendingRegistrations(pendingPanel));

        frame.setVisible(true);
    }
}