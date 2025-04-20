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

            String email = "sami@gmail.com";
            String password = "12345";
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

    /**
     * Logs in a user with the given email and password. If the login is
     * successful, the current user ID is stored in the class and the
     * corresponding User object is returned.
     *
     * @param email the email to log in with
     * @param password the password to log in with
     * @param role the role of the user to log in with (e.g. "admin" or "student")
     * @return the User object of the logged in user, or null if the login
     *         failed
     */
    public User login(String email, String password, String role) {
        // The SQL query to select the user by email and role
        String sql = "SELECT id, password_hash, role FROM users WHERE email = ? AND role = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                // Prepare the statement with the SQL query
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set the email parameter for the query
            stmt.setString(1, email);

            // Set the role parameter for the query
            stmt.setString(2, role);

            // Execute the query and get the result set
            ResultSet rs = stmt.executeQuery();

            // Check if a record was found
            if (rs.next()) {
                // Get the stored hash from the record
                String storedHash = rs.getString("password_hash");

                // Check if the given password matches the stored hash
                if (BCrypt.checkpw(password, storedHash)) {
                    // If the login is successful, store the current user ID
                    currentUserId = rs.getInt("id");

                    // Create a new User object from the record
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(email);
                    user.setRole(rs.getString("role"));

                    // Return the User object
                    return user;
                }
            }
        } catch (SQLException e) {
            // Print the stack trace if an error occurs
            e.printStackTrace();
        }
        // Return null if the login failed
        return null;
    }

    public int getCurrentUserId() {
        return currentUserId;
    }
    //We're not using this method in the current implementation, but it's here for future use.
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
    //We'ew not using this method in the current implementation, but it's here for future use.
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
    //not 
    /**
     * Resets the user's password if the provided temp password is valid
     * @param email The email address of the user to reset the password for
     * @param tempPassword The temporary password to compare against the stored temp password
     * @param newPassword The new password to set for the user
     * @return true if the password was successfully reset, false otherwise
     * @throws SQLException
     */
    public boolean resetPassword(String email, String tempPassword, String newPassword) throws SQLException {
        // Get the user's record from the database to compare the temp passwords
        String query = "SELECT * FROM users WHERE email = ? AND temp_password_expiry > NOW()";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            // Set the email parameter for the query
            stmt.setString(1, email);

            // Execute the query and get the result set
            ResultSet rs = stmt.executeQuery();

            // Check if a record was found and if the temp password matches
            if (rs.next() && BCrypt.checkpw(tempPassword, rs.getString("temp_password"))) {
                // If the temp password matches, update the user's record with the new password
                String updateQuery = "UPDATE users SET password_hash = ?, temp_password = NULL, temp_password_expiry = NULL, password_reset_required = false WHERE email = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                    // Hash the new password
                    String hashedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());

                    // Set the parameters for the update query
                    updateStmt.setString(1, hashedNewPassword);
                    updateStmt.setString(2, email);

                    // Execute the update query and check if a row was affected
                    return updateStmt.executeUpdate() > 0;
                }
            }
        }
        // If the temp password doesn't match or no record was found, return false
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