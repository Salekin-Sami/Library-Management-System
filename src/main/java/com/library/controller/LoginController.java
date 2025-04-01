package com.library.controller;

import com.library.model.Student;
import com.library.model.StudentProfile;
import com.library.model.User;
import com.library.service.AuthService;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.prefs.Preferences;

public class LoginController {
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private Button registerButton;
    @FXML
    private CheckBox rememberMeCheckbox;
    @FXML
    private VBox loadingOverlay;
    @FXML
    private Label loadingText;

    private AuthService authService = new AuthService();
    private StudentService studentService = new StudentService();
    private static final String PREF_NODE = "com.library.login";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ROLE = "role";

    @FXML
    public void initialize() {
        // Add items to the role combo box
        roleComboBox.getItems().addAll("admin", "student");
        roleComboBox.setValue("student"); // Set default value

        // Hide register button since registration will be admin-only
        registerButton.setVisible(false);
        registerButton.setManaged(false);

        // Initialize loading overlay
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);

        loadSavedCredentials();
    }

    private void loadSavedCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        String savedEmail = prefs.get(KEY_EMAIL, "");
        String savedRole = prefs.get(KEY_ROLE, "");

        if (!savedEmail.isEmpty()) {
            emailField.setText(savedEmail);
            rememberMeCheckbox.setSelected(true);

            if (!savedRole.isEmpty()) {
                roleComboBox.setValue(savedRole);
            }
        }
    }

    private void saveCredentials() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        if (rememberMeCheckbox.isSelected()) {
            prefs.put(KEY_EMAIL, emailField.getText());
            prefs.put(KEY_ROLE, roleComboBox.getValue());
        } else {
            prefs.remove(KEY_EMAIL);
            prefs.remove(KEY_ROLE);
        }
    }

    private void showLoadingOverlay(String message) {
        loadingText.setText(message);
        loadingOverlay.setVisible(true);
        loadingOverlay.setManaged(true);

        // Fade in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loadingOverlay);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void hideLoadingOverlay() {
        // Fade out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), loadingOverlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            loadingOverlay.setVisible(false);
            loadingOverlay.setManaged(false);
        });
        fadeOut.play();
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty()) {
            showAlert("Error", "Please enter both email and password.");
            return;
        }

        showLoadingOverlay("Logging in...");

        // Simulate network delay (remove in production)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate 1 second delay
                javafx.application.Platform.runLater(() -> {
                    try {
                        User user = authService.login(email, password, role);
                        if (user != null) {
                            // Save credentials if remember me is checked
                            if (rememberMeCheckbox.isSelected()) {
                                Preferences prefs = Preferences.userRoot().node(PREF_NODE);
                                prefs.put(KEY_EMAIL, email);
                                prefs.put(KEY_ROLE, user.getRole());
                                prefs.putBoolean("remember", true);
                            }

                            // Load appropriate dashboard based on role
                            String fxmlPath = user.getRole().equals("ADMIN") ? "/fxml/main.fxml"
                                    : "/fxml/student_dashboard.fxml";
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                            Parent root = loader.load();

                            if (user.getRole().equals("ADMIN")) {
                                MainController controller = loader.getController();
                                controller.setUser(user);
                            } else {
                                StudentDashboardController controller = loader.getController();
                                controller.setUser(user);
                            }

                            Stage stage = (Stage) emailField.getScene().getWindow();
                            Scene scene = new Scene(root);
                            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                            // Fade out current scene
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300),
                                    stage.getScene().getRoot());
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);

                            // Fade in new scene
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);

                            // Play transitions
                            SequentialTransition transition = new SequentialTransition(fadeOut, fadeIn);
                            transition.setOnFinished(e -> {
                                stage.setTitle("Library Management System - " + user.getRole());
                                stage.setScene(scene);
                                stage.show();
                            });
                            transition.play();
                        } else {
                            hideLoadingOverlay();
                            showAlert("Error", "Invalid email or password.");
                        }
                    } catch (Exception e) {
                        hideLoadingOverlay();
                        e.printStackTrace();
                        showAlert("Error", "Login failed: " + e.getMessage());
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_registration.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            // Fade out current scene
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            // Fade in new scene
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Play transitions
            SequentialTransition transition = new SequentialTransition(fadeOut, fadeIn);
            transition.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("Student Registration");
                stage.show();
            });
            transition.play();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load registration screen: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert("Error", "Please enter your email address!");
            return;
        }

        showLoadingOverlay("Sending reset instructions...");

        // Simulate network delay (remove in production)
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate 1 second delay
                javafx.application.Platform.runLater(() -> {
                    if (authService.sendPasswordResetEmail(email)) {
                        hideLoadingOverlay();
                        showAlert("Success", "Password reset instructions have been sent to your email.");
                    } else {
                        hideLoadingOverlay();
                        showAlert("Error", "Failed to send password reset email. Please try again.");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}