// DatabaseUtils.java
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseUtils {

    public static boolean authenticateAdmin(String username, String password) {
        String query = "SELECT password FROM admin WHERE username = ?";
        try (Connection conn = Main.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return storedHash.equals(PasswordUtils.hashPassword(password));
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean authenticateUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ? AND approved = TRUE";
        try (Connection conn = Main.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return storedHash.equals(PasswordUtils.hashPassword(password));
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changeUserPassword(String username, String oldPassword, String newPassword) {
        String query = "UPDATE users SET password = ? WHERE username = ? AND password = ?";
        try (Connection conn = Main.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String hashedOldPassword = PasswordUtils.hashPassword(oldPassword);
            String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
            stmt.setString(1, hashedNewPassword);
            stmt.setString(2, username);
            stmt.setString(3, hashedOldPassword);
            int rowsUpdated = stmt.executeUpdate();
            return rowsUpdated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}