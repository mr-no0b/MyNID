import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PendingRegistrationsLoader {
    public static void loadPendingRegistrations(JPanel pendingPanel) {
        pendingPanel.removeAll();
        try (Connection conn = Main.getConnection()) {
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

                JPanel userPanel = new JPanel(new GridBagLayout());
                userPanel.setPreferredSize(new Dimension(600, 200)); // Set fixed size for each user panel
                userPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border for better visibility

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(5, 5, 5, 5);
                gbc.fill = GridBagConstraints.HORIZONTAL;

                JLabel imageLabel = new JLabel();
                if (image != null) {
                    imageLabel.setIcon(new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT)));
                }
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridheight = 3;
                userPanel.add(imageLabel, gbc);

                gbc.gridheight = 1;
                gbc.gridx = 1;
                gbc.gridy = 0;
                userPanel.add(new JLabel("Full Name: " + fullName), gbc);

                gbc.gridy = 1;
                userPanel.add(new JLabel("Username: " + username), gbc);

                gbc.gridy = 2;
                userPanel.add(new JLabel("Date of Birth: " + dob), gbc);

                gbc.gridx = 2;
                gbc.gridy = 0;
                userPanel.add(new JLabel("Present Address: " + presentAddress), gbc);

                gbc.gridy = 1;
                userPanel.add(new JLabel("Permanent Address: " + permanentAddress), gbc);

                gbc.gridy = 2;
                userPanel.add(new JLabel("Sex: " + sex), gbc);

                gbc.gridx = 3;
                gbc.gridy = 0;
                userPanel.add(new JLabel("Phone Number: " + phoneNumber), gbc);

                JButton approveButton = new JButton("Approve");
                approveButton.setBackground(Color.GREEN); // Set background color to green
                approveButton.setForeground(Color.WHITE); // Set text color to white
                approveButton.setPreferredSize(new Dimension(100, 30)); // Set fixed size for approve button

                JButton deleteButton = new JButton("Delete");
                deleteButton.setBackground(Color.RED); // Set background color to red
                deleteButton.setForeground(Color.WHITE); // Set text color to white
                deleteButton.setPreferredSize(new Dimension(100, 30)); // Set fixed size for delete button

                approveButton.addActionListener(e -> {
                    Main.approveRegistration(username);
                    pendingPanel.remove(userPanel);
                    pendingPanel.revalidate();
                    pendingPanel.repaint();
                });

                deleteButton.addActionListener(e -> {
                    Main.deleteRegistration(username);
                    pendingPanel.remove(userPanel);
                    pendingPanel.revalidate();
                    pendingPanel.repaint();
                });

                gbc.gridx = 4;
                gbc.gridy = 0;
                userPanel.add(approveButton, gbc);

                gbc.gridy = 1;
                userPanel.add(deleteButton, gbc);

                pendingPanel.add(userPanel);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        pendingPanel.revalidate();
        pendingPanel.repaint();
    }
}