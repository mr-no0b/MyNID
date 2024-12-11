// RegistrationManager.java
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegistrationManager {
    public static void deleteRegistration(String username) {
        try (Connection conn = Main.getConnection()) {
            String deleteQuery = "DELETE FROM pending_users WHERE username = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
            deleteStmt.setString(1, username);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void approveRegistration(String username) {
        try (Connection conn = Main.getConnection()) {
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
}