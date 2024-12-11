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

    public static Connection getConnection() throws SQLException {
        Properties properties = loadDatabaseProperties();
        String url = properties.getProperty("db.url");
        String username = properties.getProperty("db.username");
        String password = properties.getProperty("db.password");
        return DriverManager.getConnection(url, username, password);
    }

    private static boolean authenticateAdmin(String username, String password) {
        return DatabaseUtils.authenticateAdmin(username, password);
    }

    private static boolean authenticateUser(String username, String password) {
        return DatabaseUtils.authenticateUser(username, password);
    }


    private static void loadPendingRegistrations(JPanel pendingPanel) {
        PendingRegistrationsLoader.loadPendingRegistrations(pendingPanel);
    }

    public static void deleteRegistration(String username) {
        RegistrationManager.deleteRegistration(username);
    }

    public static void approveRegistration(String username) {
        RegistrationManager.approveRegistration(username);
    }

    private static void loadUserProfile(String username, JPanel profilePanel) {
        String query = "SELECT full_name, username, dob, present_address, permanent_address, sex, phone_number, image FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                profilePanel.removeAll();
                profilePanel.setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(10, 10, 10, 10);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JLabel imageLabel = new JLabel();
                String image = rs.getString("image");
                if (image != null) {
                    imageLabel.setIcon(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(150, 150, Image.SCALE_DEFAULT)));
                }
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridheight = 3;
                profilePanel.add(imageLabel, gbc);

                gbc.gridheight = 1;
                gbc.gridx = 1;
                gbc.gridy = 0;
                profilePanel.add(createLabel("Full Name: ", rs.getString("full_name")), gbc);

                gbc.gridy = 1;
                profilePanel.add(createLabel("Username: ", rs.getString("username")), gbc);

                gbc.gridy = 2;
                profilePanel.add(createLabel("Date of Birth: ", rs.getString("dob")), gbc);

                gbc.gridy = 3;
                profilePanel.add(createLabel("Present Address: ", rs.getString("present_address")), gbc);

                gbc.gridy = 4;
                profilePanel.add(createLabel("Permanent Address: ", rs.getString("permanent_address")), gbc);

                gbc.gridy = 5;
                profilePanel.add(createLabel("Sex: ", rs.getString("sex")), gbc);

                gbc.gridy = 6;
                profilePanel.add(createLabel("Phone Number: ", rs.getString("phone_number")), gbc);

                // Add back button
                JButton backButton = new JButton("Back");
                gbc.gridx = 0;
                gbc.gridy = 7;
                gbc.gridwidth = 1;
                profilePanel.add(backButton, gbc);

                // Add change password button
                JButton changePasswordButton = new JButton("Change Password");
                gbc.gridx = 1;
                gbc.gridy = 7;
                profilePanel.add(changePasswordButton, gbc);

                backButton.addActionListener(e -> {
                    CardLayout cl = (CardLayout) profilePanel.getParent().getLayout();
                    cl.show(profilePanel.getParent(), "UserLogin");
                });

                changePasswordButton.addActionListener(e -> {
                    JPanel changePasswordPanel = new JPanel(new GridBagLayout());
                    GridBagConstraints c = new GridBagConstraints();
                    c.insets = new Insets(10, 10, 10, 10);
                    c.fill = GridBagConstraints.HORIZONTAL;

                    JPasswordField oldPasswordField = new JPasswordField();
                    JPasswordField newPasswordField = new JPasswordField();
                    JPasswordField confirmPasswordField = new JPasswordField();
                    JButton submitButton = new JButton("Submit");

                    c.gridx = 0;
                    c.gridy = 0;
                    changePasswordPanel.add(new JLabel("Old Password:"), c);
                    c.gridx = 1;
                    changePasswordPanel.add(oldPasswordField, c);

                    c.gridx = 0;
                    c.gridy = 1;
                    changePasswordPanel.add(new JLabel("New Password:"), c);
                    c.gridx = 1;
                    changePasswordPanel.add(newPasswordField, c);

                    c.gridx = 0;
                    c.gridy = 2;
                    changePasswordPanel.add(new JLabel("Confirm Password:"), c);
                    c.gridx = 1;
                    changePasswordPanel.add(confirmPasswordField, c);

                    c.gridx = 1;
                    c.gridy = 3;
                    changePasswordPanel.add(submitButton, c);

                    profilePanel.removeAll();
                    profilePanel.add(changePasswordPanel);
                    profilePanel.revalidate();
                    profilePanel.repaint();

                    submitButton.addActionListener(ev -> {
                        String oldPassword = new String(oldPasswordField.getPassword());
                        String newPassword = new String(newPasswordField.getPassword());
                        String confirmPassword = new String(confirmPasswordField.getPassword());

                        if (!newPassword.equals(confirmPassword)) {
                            JOptionPane.showMessageDialog(profilePanel, "New passwords do not match", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        if (changeUserPassword(username, oldPassword, newPassword)) {
                            JOptionPane.showMessageDialog(profilePanel, "Password changed successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                            loadUserProfile(username, profilePanel);
                        } else {
                            JOptionPane.showMessageDialog(profilePanel, "Old password is incorrect", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    });
                });

                profilePanel.revalidate();
                profilePanel.repaint();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean changeUserPassword(String username, String oldPassword, String newPassword) {
        return DatabaseUtils.changeUserPassword(username, oldPassword, newPassword);
    }

    private static JPanel createLabel(String labelName, String labelValue) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel nameLabel = new JLabel(labelName);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        JLabel valueLabel = new JLabel(labelValue);
        valueLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        panel.add(nameLabel);
        panel.add(valueLabel);
        return panel;
    }
    // Add placeholder text and focus listener to text fields
    private static void addPlaceholderText(JTextField textField, String placeholder) {
        textField.setText(placeholder);
        textField.setForeground(Color.GRAY);
        textField.setFont(new Font("Arial", Font.ITALIC, 12));
        if (textField instanceof JPasswordField) {
            ((JPasswordField) textField).setEchoChar((char) 0);
        }
        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                    textField.setFont(new Font("Arial", Font.PLAIN, 18));
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar('*');
                    }
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                    textField.setFont(new Font("Arial", Font.ITALIC, 12));
                    if (textField instanceof JPasswordField) {
                        ((JPasswordField) textField).setEchoChar((char) 0);
                    }
                }
            }
        });
    }
    private static JFormattedTextField createDobField() {
        MaskFormatter dateMask = null;
        try {
            dateMask = new MaskFormatter("##-##-####");
            dateMask.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JFormattedTextField dobField = new JFormattedTextField(dateMask);
        dobField.setText("DD-MM-YYYY"); // Set the initial text to the placeholder format
        dobField.setForeground(Color.GRAY);
        dobField.setFont(new Font("Arial", Font.ITALIC, 15));

        dobField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (dobField.getText().equals("DD-MM-YYYY")) {
                    dobField.setText("");
                    dobField.setForeground(Color.BLACK);
                    dobField.setFont(new Font("Arial", Font.PLAIN, 18));
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (dobField.getText().isEmpty()) {
                    dobField.setText("DD-MM-YYYY");
                    dobField.setForeground(Color.GRAY);
                    dobField.setFont(new Font("Arial", Font.ITALIC, 15));
                }
            }
        });

        return dobField;
    }
    private static void clearFields(JTextField... fields) {
        for (JTextField field : fields) {
            field.setText("");
            field.setForeground(Color.BLACK);
            field.setFont(new Font("Arial", Font.PLAIN, 18));
            if (field instanceof JPasswordField) {
                ((JPasswordField) field).setEchoChar('*');
            }
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
        // Set the frame to not resizable
        frame.setResizable(false);

// Set the application name to "MyNID"
        frame.setTitle("MyNID");
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

        // Add logo and resize it
        ImageIcon logoIcon = new ImageIcon("res/logo.png");
        Image logoImage = logoIcon.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH); // Resize the logo
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage));
        logoLabel.setHorizontalAlignment(SwingConstants.LEFT);
        topPanel.add(logoLabel, BorderLayout.WEST);

        // Add weather information
        JLabel weatherLabel = new JLabel();
        weatherLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        topPanel.add(weatherLabel, BorderLayout.EAST);

        // Fetch and display weather information
        SwingUtilities.invokeLater(() -> {
            String temperature = WeatherFetcher.getTemperature("Khulna"); // Replace "Khulna" with your desired city
            weatherLabel.setText(temperature);
            weatherLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Set font size to 24
            weatherLabel.setForeground(Color.WHITE); // Set font color to white

            // Load and resize weather icon
            ImageIcon weatherIcon = new ImageIcon("res/weather-icon.png"); // Replace with your weather icon path
            Image weatherImage = weatherIcon.getImage(); // Resize the icon to 50x50
            weatherLabel.setIcon(new ImageIcon(weatherImage));
            weatherLabel.setIconTextGap(10); // Add some gap between icon and text

            // Add padding to shift the label to the right
            weatherLabel.setBorder(new EmptyBorder(0, 0, 0, 20)); // Add padding to the right
        });

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
        // Update the font size of the login label
        // Update the font size, style, and color of the login label
        loginLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 30)); // Set font size to 20, bold and italic
        loginLabel.setForeground(new Color(9, 9, 56)); // Set text color to a stylish blue

// Update the style of the login button
        loginButton.setFont(new Font("Arial", Font.BOLD, 14)); // Set font size to 14
        loginButton.setBackground(Color.BLUE); // Set background color to blue
        loginButton.setForeground(Color.WHITE); // Set text color to white

// Update the style of the administration button
        adminLoginButton.setFont(new Font("Arial", Font.BOLD, 14)); // Set font size to 14
        adminLoginButton.setBackground(Color.RED); // Set background color to red
        loginButton.setPreferredSize(new Dimension(200, 50));
        adminLoginButton.setForeground(Color.WHITE); // Set text color to white
        adminLoginButton.setPreferredSize(new Dimension(200, 50));
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(30,30,255)); // Change background color on hover
                loginButton.setForeground(Color.WHITE); // Change text color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(Color.BLUE); // Reset background color when not hovered
                loginButton.setForeground(Color.WHITE); // Reset text color when not hovered
            }
        });

// Update the style of the administration button
        adminLoginButton.setFont(new Font("Arial", Font.BOLD, 14)); // Set font size to 14
        adminLoginButton.setBackground(Color.RED); // Set background color to red
        adminLoginButton.setForeground(Color.WHITE); // Set text color to white
        adminLoginButton.setPreferredSize(new Dimension(200, 50)); // Set preferred size

// Add hover effect to the administration button
        adminLoginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                adminLoginButton.setBackground(new Color(255, 30, 30)); // Change background color on hover
                adminLoginButton.setForeground(Color.WHITE); // Change text color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                adminLoginButton.setBackground(Color.RED); // Reset background color when not hovered
                adminLoginButton.setForeground(Color.WHITE); // Reset text color when not hovered
            }
        });
        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(loginLabel, gbc);
        gbc.gridy = 1;
        leftPanel.add(loginButton, gbc);
        gbc.gridy = 2;
        leftPanel.add(adminLoginButton, gbc);

        homePanel.add(leftPanel);

        // Right section for registration
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

        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel registrationLabel = new JLabel("Registration");
        registrationLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 35)); // Increase font size
//        registrationLabel.setBorder(new EmptyBorder(0, 300, 0, 0));
        JTextField fullNameField = new JTextField();
        fullNameField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        fullNameField.setPreferredSize(new Dimension(300, 20)); // Increase size
        fullNameField.setMinimumSize(new Dimension(300, 30)); // Enforce minimum size
        fullNameField.setMaximumSize(new Dimension(300, 30));
        JTextField usernameField = new JTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        usernameField.setPreferredSize(new Dimension(300, 40)); // Increase size

        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        passwordField.setPreferredSize(new Dimension(300, 40)); // Increase size
        passwordField.setEchoChar('*'); // Set echo character to '*'

// Add placeholder text and focus listener to password field
        passwordField.setText("Enter your password");
        passwordField.setForeground(Color.GRAY);
        passwordField.setFont(new Font("Arial", Font.ITALIC, 12));
        passwordField.setEchoChar((char) 0); // Temporarily disable echo character for placeholder

        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (passwordField.getText().equals("Enter your password")) {
                    passwordField.setText("");
                    passwordField.setForeground(Color.BLACK);
                    passwordField.setFont(new Font("Arial", Font.PLAIN, 18));
                    passwordField.setEchoChar('*'); // Enable echo character when focused
                }
            }

            public void focusLost(java.awt.event.FocusEvent evt) {
                if (passwordField.getText().isEmpty()) {
                    passwordField.setText("Enter your password");
                    passwordField.setForeground(Color.GRAY);
                    passwordField.setFont(new Font("Arial", Font.ITALIC, 12));
                    passwordField.setEchoChar((char) 0); // Disable echo character for placeholder
                }
            }
        });

        JFormattedTextField dobField = createDobField();
        dobField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        dobField.setPreferredSize(new Dimension(300, 40)); // Increase size

        JTextField presentAddressField = new JTextField();
        presentAddressField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        presentAddressField.setPreferredSize(new Dimension(300, 40)); // Increase size

        JTextField permanentAddressField = new JTextField();
        permanentAddressField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        permanentAddressField.setPreferredSize(new Dimension(300, 40)); // Increase size

        String[] sexOptions = {"Male", "Female"};
        JComboBox<String> sexComboBox = new JComboBox<>(sexOptions);
        sexComboBox.setFont(new Font("Arial", Font.PLAIN, 13)); // Increase font size
        sexComboBox.setPreferredSize(new Dimension(300, 40)); // Increase size

        JTextField phoneNumberField = new JTextField();
        phoneNumberField.setFont(new Font("Arial", Font.PLAIN, 18)); // Increase font size
        phoneNumberField.setPreferredSize(new Dimension(300, 40)); // Increase size

        JLabel imageLabel = new JLabel();
        JLabel imageDisplay = new JLabel();
        imageDisplay.setPreferredSize(new Dimension(70, 100)); // Square display for image
        //imageDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border

        JButton imageButton = new JButton("Select Image");
        JButton registerButton = new JButton("Signup");
        // Apply placeholder text to registration fields
        addPlaceholderText(fullNameField, "Enter your full name");
        addPlaceholderText(usernameField, "Enter your username");
        addPlaceholderText(passwordField, "Enter your password");
        addPlaceholderText(dobField, "YYYY-MM-DD");
        addPlaceholderText(presentAddressField, "Enter your present address");
        addPlaceholderText(permanentAddressField, "Enter your permanent address");
        //addPlaceholderText(sexField, "Enter your sex");
        addPlaceholderText(phoneNumberField, "Enter your phone number");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14)); // Set font size to 14
        registerButton.setBackground(Color.GREEN); // Set background color to green
        registerButton.setForeground(Color.WHITE); // Set text color to white
        registerButton.setPreferredSize(new Dimension(200, 50)); // Set preferred size
        gbc.gridx = 1;

        gbc.gridy = 0;
        rightPanel.add(registrationLabel, gbc);
        gbc.gridx=0;
        gbc.gridy = 1;
        JLabel fullNameLabel = new JLabel("Full Name");
        fullNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(fullNameLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(fullNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel dobLabel = new JLabel("Date of Birth");
        dobLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(dobLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(dobField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel presentAddressLabel = new JLabel("Present Address");
        presentAddressLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(presentAddressLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(presentAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        JLabel permanentAddressLabel = new JLabel("Permanent Address");
        permanentAddressLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(permanentAddressLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(permanentAddressField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        JLabel sexLabel = new JLabel("Sex");
        sexLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(sexLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(sexComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 8;
        JLabel phoneNumberLabel = new JLabel("Phone Number");
        phoneNumberLabel.setFont(new Font("Arial", Font.BOLD, 18));
        rightPanel.add(phoneNumberLabel, gbc);
        gbc.gridx = 1;
        rightPanel.add(phoneNumberField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 9;
        rightPanel.add(imageDisplay, gbc); // Add image display
        gbc.gridx = 1;
        rightPanel.add(imageButton, gbc);

        // Add an empty label to create space above the signup button
        gbc.gridx = 0;
        gbc.gridy = 29; // Adjust the row index to create space
        rightPanel.add(new JLabel(" "), gbc); // Empty label for spacing

        gbc.gridx = 0;
        gbc.gridy = 20;
        gbc.gridwidth = 2; // Span across two columns
        gbc.anchor = GridBagConstraints.CENTER; // Center the button
        rightPanel.add(registerButton, gbc);

        homePanel.add(rightPanel);



        // Admin login panel
        // Admin login panel
        JPanel adminLoginPanel = new JPanel(new GridBagLayout());
        adminLoginPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField adminUsernameField = new JTextField();
        adminUsernameField.setFont(new Font("Arial", Font.PLAIN, 18)); // Set font size
        adminUsernameField.setPreferredSize(new Dimension(300, 30)); // Set preferred size

        JPasswordField adminPasswordField = new JPasswordField();
        adminPasswordField.setFont(new Font("Arial", Font.PLAIN, 18)); // Set font size
        adminPasswordField.setPreferredSize(new Dimension(300, 30)); // Set preferred size

        JButton adminLoginSubmitButton = new JButton("Login");
        adminLoginSubmitButton.setFont(new Font("Arial", Font.BOLD, 17)); // Set font size
        adminLoginSubmitButton.setBackground(Color.BLUE); // Set background color to blue
        adminLoginSubmitButton.setForeground(Color.WHITE); // Set text color to white

        JButton adminBackButton = new JButton("Back");
        adminBackButton.setFont(new Font("Arial", Font.BOLD, 17)); // Set font size
        adminBackButton.setBackground(Color.GRAY); // Set background color to yellow
        adminBackButton.setForeground(Color.BLACK);

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
        userUsernameField.setFont(new Font("Arial", Font.PLAIN, 18)); // Set font size
        userUsernameField.setPreferredSize(new Dimension(300, 30)); // Set preferred size

        JPasswordField userPasswordField = new JPasswordField();
        userPasswordField.setFont(new Font("Arial", Font.PLAIN, 18)); // Set font size
        userPasswordField.setPreferredSize(new Dimension(300, 30)); // Set preferred size

        JButton userLoginSubmitButton = new JButton("Login");
        userLoginSubmitButton.setFont(new Font("Arial", Font.BOLD, 17)); // Set font size
        userLoginSubmitButton.setBackground(Color.BLUE); // Set background color to blue
        userLoginSubmitButton.setForeground(Color.WHITE); // Set text color to white

        JButton userBackButton = new JButton("Back");
        userBackButton.setFont(new Font("Arial", Font.BOLD, 17)); // Set font size
        userBackButton.setBackground(Color.GRAY); // Set background color to yellow
        userBackButton.setForeground(Color.BLACK);

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

        adminLoginButton.addActionListener(e -> {
            clearFields(adminUsernameField, adminPasswordField);
            cl.show(mainPanel, "AdminLogin");
        });

        adminLoginSubmitButton.addActionListener(e -> {

            String username = adminUsernameField.getText();
            String password = new String(adminPasswordField.getPassword());
            if (authenticateAdmin(username, password)) {
                loadPendingRegistrations(pendingPanel);
                cl.show(mainPanel, "Admin");
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid admin credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
            clearFields(adminUsernameField, adminPasswordField);
        });

        adminBackButton.addActionListener(e -> cl.show(mainPanel, "Home"));
        adminApprovalBackButton.addActionListener(e -> cl.show(mainPanel, "AdminLogin"));

        loginButton.addActionListener(e -> {
            clearFields(userUsernameField, userPasswordField);
            cl.show(mainPanel, "UserLogin");
        });

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
            String sex = (String) sexComboBox.getSelectedItem();
            String phoneNumber = phoneNumberField.getText();
            String image = imageLabel.getText();

            if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || dob.isEmpty() ||
                    presentAddress.isEmpty() || permanentAddress.isEmpty() || sex.isEmpty() ||
                    phoneNumber.isEmpty() || image.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "All fields are required", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RegistrationHandler.handleRegistration(fullName, username, password, dob, presentAddress, permanentAddress, sex, phoneNumber, image);
            clearFields(fullNameField, usernameField, passwordField, dobField, presentAddressField, permanentAddressField, phoneNumberField);
        });


        // Adjust the size of the image display to passport size (35mm x 45mm)
        // Adjust the size of the image display to passport size (35mm x 45mm)
        // Adjust the size of the image display to a square size (e.g., 100x100)
        // Adjust the size of the image display to a square size (e.g., 100x100)
        imageDisplay.setPreferredSize(new Dimension(100, 100)); // Square display for image
        imageDisplay.setMaximumSize(new Dimension(100, 100)); // Ensure it stays square
        imageDisplay.setMinimumSize(new Dimension(100, 100)); // Ensure it stays square
        //imageDisplay.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border to indicate the box

        imageButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(frame);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedFile.getAbsolutePath()).getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH));
                imageDisplay.setIcon(imageIcon);
                imageDisplay.setText(""); // Clear any previous text
                imageLabel.setText(selectedFile.getAbsolutePath());
            }
        });

        refreshButton.addActionListener(e -> loadPendingRegistrations(pendingPanel));

        frame.setVisible(true);
    }
}