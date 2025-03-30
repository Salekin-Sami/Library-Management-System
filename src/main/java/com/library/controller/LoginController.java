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
import javafx.stage.Stage;
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

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();
        String role = roleComboBox.getValue();

        if (email.isEmpty() || password.isEmpty() || role == null) {
            showAlert("Error", "Please fill in all fields!");
            return;
        }

        if ("admin".equals(role)) {
            handleAdminLogin(email, password);
        } else {
            handleStudentLogin(email, password);
        }
    }

    private void handleAdminLogin(String email, String password) {
        if (authService.login(email, password, "admin")) {
            saveCredentials();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main.fxml"));
                Parent root = loader.load();
                MainController controller = loader.getController();

                // Create and set the user object
                User user = new User();
                user.setEmail(email);
                user.setRole("admin");
                controller.setUser(user);

                Stage stage = (Stage) emailField.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                stage.setScene(scene);
                stage.setTitle("Library Management System - Admin Dashboard");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load admin dashboard: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Invalid email or password!");
        }
    }

    private void handleStudentLogin(String email, String password) {
        if (authService.login(email, password, "student")) {
            saveCredentials();
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_dashboard.fxml"));
                Parent root = loader.load();
                StudentDashboardController controller = loader.getController();

                // Get student details from the database
                Student student = studentService.getStudentByEmail(email);
                if (student != null) {
                    controller.setCurrentStudent(student);

                    Stage stage = (Stage) emailField.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

                    stage.setScene(scene);
                    stage.setTitle("Library Management System - Student Dashboard");
                    stage.show();
                } else {
                    showAlert("Error", "Student profile not found!");
                }
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load student dashboard: " + e.getMessage());
            }
        } else {
            showAlert("Error", "Invalid email or password!");
        }
    }

    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_registration.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) emailField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("Student Registration");
            stage.show();
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

        if (authService.sendPasswordResetEmail(email)) {
            showAlert("Success", "Password reset instructions have been sent to your email.");
        } else {
            showAlert("Error", "Failed to send password reset email. Please try again.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}