package com.library.controller;

import com.library.model.User;
import com.library.service.AuthService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;
    @FXML
    private CheckBox rememberMeCheckbox;

    private final AuthService authService = new AuthService();
    private static final String PREF_NODE = "com.library.login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER = "remember";

    @FXML
    public void initialize() {
        loadSavedCredentials();
    }

    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        boolean remember = prefs.getBoolean(KEY_REMEMBER, false);
        if (remember) {
            String savedEmail = prefs.get(KEY_EMAIL, "");
            String savedPassword = prefs.get(KEY_PASSWORD, "");
            emailField.setText(savedEmail);
            passwordField.setText(savedPassword);
            rememberMeCheckbox.setSelected(true);
        }
    }

    private void saveCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        if (rememberMeCheckbox.isSelected()) {
            prefs.put(KEY_EMAIL, emailField.getText());
            prefs.put(KEY_PASSWORD, passwordField.getText());
            prefs.putBoolean(KEY_REMEMBER, true);
        } else {
            prefs.remove(KEY_EMAIL);
            prefs.remove(KEY_PASSWORD);
            prefs.putBoolean(KEY_REMEMBER, false);
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        try {
            User user = authService.login(email, password);
            if (user != null) {
                if (user.isPasswordResetRequired()) {
                    showPasswordResetDialog(email);
                } else {
                    // Save credentials if Remember Me is checked
                    saveCredentials();

                    // Navigate to main view
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                    Parent root = loader.load();
                    MainController mainController = loader.getController();
                    mainController.setUser(user);

                    Stage stage = (Stage) emailField.getScene().getWindow();
                    stage.setTitle("Library Management System");
                    stage.setScene(new Scene(root));
                    stage.show();
                }
            } else {
                messageLabel.setText("Invalid email or password.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        } catch (Exception e) {
            showAlert("Error", "Failed to load main view: " + e.getMessage());
        }
    }

    private void showPasswordResetDialog(String email) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/password_reset_dialog.fxml"));
            Parent root = loader.load();
            PasswordResetDialogController controller = loader.getController();
            controller.setUserEmail(email);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Reset Password");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            showAlert("Error", "Failed to open password reset dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Error", "Please enter a valid email address.");
            return;
        }

        try {
            if (authService.register(email, password)) {
                showAlert("Success", "Registration successful! You can now login.");
                messageLabel.setText("");
            } else {
                messageLabel.setText("Registration failed. Email might already be registered.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Error", "Please enter a valid email address.");
            return;
        }

        try {
            if (authService.requestPasswordReset(email)) {
                showAlert("Success", "A temporary password has been sent to your email.");
                messageLabel.setText("");
            } else {
                messageLabel.setText("Email not found in the system.");
            }
        } catch (SQLException e) {
            showAlert("Error", "Database error: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}