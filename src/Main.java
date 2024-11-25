import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.text.ParseException;

public class Main {
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "12345678";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC Driver not found", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFrame frame = new JFrame("User Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full-sized window
        frame.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new CardLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        JPanel loginPanel = new JPanel(new GridLayout(3, 2));
        JTextField loginUsernameField = new JTextField();
        JPasswordField loginPasswordField = new JPasswordField();
        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Signup");

        loginPanel.add(new JLabel("Username:"));
        loginPanel.add(loginUsernameField);
        loginPanel.add(new JLabel("Password:"));
        loginPanel.add(loginPasswordField);
        loginPanel.add(loginButton);
        loginPanel.add(signupButton);

        mainPanel.add(loginPanel, "Login");

        JPanel signupPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

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
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back");

        // Image display field above the select image button
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        signupPanel.add(imageDisplay, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        signupPanel.add(imageButton, gbc);

        gbc.anchor = GridBagConstraints.CENTER;
        gbc.gridheight = 1;

        gbc.gridx = 0;
        gbc.gridy = 0;
        signupPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        signupPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        signupPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        signupPanel.add(new JLabel("Date of Birth:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(dobField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        signupPanel.add(new JLabel("Present Address:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(presentAddressField, gbc);


        gbc.gridx = 0;
        gbc.gridy = 5;
        signupPanel.add(new JLabel("Permanent Address:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(permanentAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        signupPanel.add(new JLabel("Sex:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(sexField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        signupPanel.add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        signupPanel.add(phoneNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        signupPanel.add(registerButton, gbc);
        gbc.gridx = 1;
        signupPanel.add(backButton, gbc);

        mainPanel.add(signupPanel, "Signup");

        JPanel adminPanel = new JPanel(new BorderLayout());
        JPanel pendingPanel = new JPanel(new GridLayout(0, 1));
        JButton refreshButton = new JButton("Refresh");
        JButton adminBackButton = new JButton("Back");
        adminPanel.add(new JScrollPane(pendingPanel), BorderLayout.CENTER);
        adminPanel.add(refreshButton, BorderLayout.SOUTH);
        adminPanel.add(adminBackButton, BorderLayout.NORTH);

        mainPanel.add(adminPanel, "Admin");

        JPanel profilePanel = new JPanel();
        mainPanel.add(profilePanel, "Profile");

        JButton adminLoginButton = new JButton("Admin Login");
        frame.add(adminLoginButton, BorderLayout.NORTH);

        CardLayout cl = (CardLayout) mainPanel.getLayout();

        adminLoginButton.addActionListener(e -> {
            String username = JOptionPane.showInputDialog(frame, "Enter admin username:");
            String password = JOptionPane.showInputDialog(frame, "Enter admin password:");
            if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
                cl.show(mainPanel, "Admin");
                loadPendingRegistrations(pendingPanel);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid admin credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        loginButton.addActionListener(e -> {
            String username = loginUsernameField.getText();
            String password = new String(loginPasswordField.getPassword());
            if (authenticateUser(username, password)) {
                loadUserProfile(username, profilePanel);
                cl.show(mainPanel, "Profile");
                JOptionPane.showMessageDialog(frame, "Login successful", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        signupButton.addActionListener(e -> cl.show(mainPanel, "Signup"));

        backButton.addActionListener(e -> cl.show(mainPanel, "Login"));

        adminBackButton.addActionListener(e -> cl.show(mainPanel, "Login"));

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

        refreshButton.addActionListener(e -> loadPendingRegistrations(pendingPanel));

        frame.setVisible(true);
    }

    private static void handleRegistration(String fullName, String username, String password, String dob, String presentAddress, String permanentAddress, String sex, String phoneNumber, String image) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO pending_users (full_name, username, password, dob, present_address, permanent_address, sex, phone_number, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/login", "root", "12345678");
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/login", "root", "12345678")) {
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
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/login", "root", "12345678")) {
            // Check if username already exists
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

    private static boolean authenticateUser(String username, String password) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/login", "root", "12345678")) {
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


    private static void loadUserProfile(String username, JPanel profilePanel) {
        String query = "SELECT full_name, username, dob, present_address, permanent_address, sex, phone_number, image FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/login", "root", "12345678");
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
}
