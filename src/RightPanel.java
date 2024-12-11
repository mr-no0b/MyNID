// RightPanel.java
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;

public class RightPanel extends JPanel {
    public JTextField fullNameField;
    public JTextField usernameField;
    public JPasswordField passwordField;
    public JFormattedTextField dobField;
    public JTextField presentAddressField;
    public JTextField permanentAddressField;
    public JComboBox<String> sexComboBox;
    public JTextField phoneNumberField;
    public JLabel imageLabel;
    public JLabel imageDisplay;
    public JButton imageButton;
    public JButton registerButton;
    private Image backgroundImage = new ImageIcon("res/right-bg.png").getImage();

    public RightPanel() {
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel registrationLabel = new JLabel("Registration");
        registrationLabel.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 35));
        fullNameField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        dobField = createDobField();
        presentAddressField = new JTextField();
        permanentAddressField = new JTextField();
        String[] sexOptions = {"Male", "Female"};
        sexComboBox = new JComboBox<>(sexOptions);
        phoneNumberField = new JTextField();
        imageLabel = new JLabel();
        imageDisplay = new JLabel();
        imageButton = new JButton("Select Image");
        registerButton = new JButton("Signup");
        registerButton.setFont(new Font("Arial", Font.BOLD, 14)); // Set font size to 14
        registerButton.setBackground(Color.GREEN); // Set background color to green
        registerButton.setForeground(Color.WHITE); // Set text color to white
        registerButton.setPreferredSize(new Dimension(100, 30));
        JLabel fullNameLabel = new JLabel("Full Name");
        fullNameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel dobLabel = new JLabel("Date of Birth");
        dobLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel presentAddressLabel = new JLabel("Present Address");
        presentAddressLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel permanentAddressLabel = new JLabel("Permanent Address");
        permanentAddressLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel sexLabel = new JLabel("Sex");
        sexLabel.setFont(new Font("Arial", Font.BOLD, 18));
        JLabel phoneNumberLabel = new JLabel("Phone Number");
        phoneNumberLabel.setFont(new Font("Arial", Font.BOLD, 18));

        gbc.gridx = 1;
        gbc.gridy = 0;
        add(registrationLabel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(fullNameLabel, gbc);
        gbc.gridx = 1;
        add(fullNameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(usernameLabel, gbc);
        gbc.gridx = 1;
        add(usernameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        add(passwordLabel, gbc);
        gbc.gridx = 1;
        add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        add(dobLabel, gbc);
        gbc.gridx = 1;
        add(dobField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        add(presentAddressLabel, gbc);
        gbc.gridx = 1;
        add(presentAddressField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        add(permanentAddressLabel, gbc);
        gbc.gridx = 1;
        add(permanentAddressField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 7;
        add(sexLabel, gbc);
        gbc.gridx = 1;
        add(sexComboBox, gbc);
        gbc.gridx = 0;
        gbc.gridy = 8;
        add(phoneNumberLabel, gbc);
        gbc.gridx = 1;
        add(phoneNumberField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 9;
        add(imageDisplay, gbc);
        gbc.gridx = 1;
        add(imageButton, gbc);
        gbc.gridx = 0;
        gbc.gridy = 20;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(registerButton, gbc);

        // Add placeholders
        Main.addPlaceholderText(fullNameField, "Enter your full name");
        Main.addPlaceholderText(usernameField, "Enter your username");
        Main.addPlaceholderText(passwordField, "Enter your password");
        Main.addPlaceholderText(dobField, "DD-MM-YYYY");
        Main.addPlaceholderText(presentAddressField, "Enter your present address");
        Main.addPlaceholderText(permanentAddressField, "Enter your permanent address");
        Main.addPlaceholderText(phoneNumberField, "Enter your phone number");
    }

    private JFormattedTextField createDobField() {
        MaskFormatter dateMask = null;
        try {
            dateMask = new MaskFormatter("##-##-####");
            dateMask.setPlaceholderCharacter('_');
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JFormattedTextField dobField = new JFormattedTextField(dateMask);
        dobField.setText("DD-MM-YYYY");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f)); // Set opacity to 30%
            g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
            g2d.dispose();
        }
    }
}