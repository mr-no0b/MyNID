// RegistrationHandler.java
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationHandler {
    public static void handleRegistration(String fullName, String username, String password, String dob, String presentAddress, String permanentAddress, String sex, String phoneNumber, String image) {
        String checkQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
        String insertQuery = "INSERT INTO pending_users (full_name, username, password, dob, present_address, permanent_address, sex, phone_number, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Main.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(null, "Username already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Convert date from DD-MM-YYYY to YYYY-MM-DD
            String[] dobParts = dob.split("-");
            String formattedDob = dobParts[2] + "-" + dobParts[1] + "-" + dobParts[0];

            String hashedPassword = PasswordUtils.hashPassword(password);

            insertStmt.setString(1, fullName);
            insertStmt.setString(2, username);
            insertStmt.setString(3, hashedPassword);
            insertStmt.setString(4, formattedDob);
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
}