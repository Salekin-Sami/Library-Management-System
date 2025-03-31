package com.library.service;

import com.library.model.User;
import com.library.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

public class AuthService {
    private int currentUserId;

    public void createTestAdmin() throws SQLException {
        String query = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'admin') ON DUPLICATE KEY UPDATE password_hash = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            String email = "sirajussalekin23522@gmail.com";
            String password = "admin123";
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, hashedPassword);

            stmt.executeUpdate();
            System.out.println("Test admin account created/updated successfully!");
            System.out.println("Email: " + email);
            System.out.println("Password: " + password);
        }
    }

    public User login(String email, String password, String role) {
        String sql = "SELECT id, password_hash, role FROM users WHERE email = ? AND role = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, role);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (BCrypt.checkpw(password, storedHash)) {
                    currentUserId = rs.getInt("id");
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(email);
                    user.setRole(rs.getString("role"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }

    public boolean register(String email, String password) throws SQLException {
        String query = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'student')";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean requestPasswordReset(String email) throws SQLException {
        String tempPassword = generateTempPassword();
        String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt());
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(24);

        String query = "UPDATE users SET temp_password = ?, temp_password_expiry = ?, password_reset_required = true WHERE email = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, hashedTempPassword);
            stmt.setTimestamp(2, Timestamp.valueOf(expiryTime));
            stmt.setString(3, email);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                // TODO: Send email with temp password
                return true;
            }
        }
        return false;
    }

    public boolean resetPassword(String email, String tempPassword, String newPassword) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ? AND temp_password_expiry > NOW()";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next() && BCrypt.checkpw(tempPassword, rs.getString("temp_password"))) {
                String updateQuery = "UPDATE users SET password_hash = ?, temp_password = NULL, temp_password_expiry = NULL, password_reset_required = false WHERE email = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
                    updateStmt.setString(1, hashedNewPassword);
                    updateStmt.setString(2, email);
                    return updateStmt.executeUpdate() > 0;
                }
            }
        }
        return false;
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder tempPassword = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 10; i++) {
            tempPassword.append(chars.charAt(random.nextInt(chars.length())));
        }

        return tempPassword.toString();
    }

    public boolean sendPasswordResetEmail(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        String updateSql = "UPDATE users SET temp_password = ?, temp_password_expiry = ?, password_reset_required = TRUE WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection()) {
            // First, get the user ID
            int userId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, email);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("id");
                }
            }

            if (userId != -1) {
                // Generate a temporary password
                String tempPassword = generateTemporaryPassword();
                String hashedTempPassword = BCrypt.hashpw(tempPassword, BCrypt.gensalt());

                // Update the user's temporary password
                try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
                    stmt.setString(1, hashedTempPassword);
                    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24 hours
                                                                                                           // expiry
                    stmt.setInt(3, userId);
                    stmt.executeUpdate();
                }

                // TODO: Send email with temporary password
                // For now, we'll just print it to console
                System.out.println("Temporary password for " + email + ": " + tempPassword);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String generateTemporaryPassword() {
        // Generate a random 8-character password
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        return password.toString();
    }
}