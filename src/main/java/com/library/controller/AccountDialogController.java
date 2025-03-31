package com.library.controller;

import com.library.model.Student;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.sql.SQLException;

public class AccountDialogController {
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField studentIdField;
    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;

    private Student student;
    private Stage dialogStage;
    private final StudentService studentService;

    public AccountDialogController() {
        this.studentService = new StudentService();
    }

    public void setStudent(Student student) {
        this.student = student;
        populateFields();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    private void populateFields() {
        nameField.setText(student.getName());
        emailField.setText(student.getEmail());
        contactField.setText(student.getContactNumber());
        studentIdField.setText(student.getStudentId());
    }

    @FXML
    private void handleUpdateInfo() {
        if (validateInput()) {
            try {
                student.setName(nameField.getText().trim());
                student.setEmail(emailField.getText().trim());
                student.setContactNumber(contactField.getText().trim());

                studentService.updateStudent(student);
                showAlert("Success", "Account information updated successfully!", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to update account information: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleChangePassword() {
        if (validatePasswordChange()) {
            try {
                studentService.updateStudentPassword(student.getId(), newPasswordField.getText());
                showAlert("Success", "Password changed successfully!", Alert.AlertType.INFORMATION);
                clearPasswordFields();
            } catch (SQLException e) {
                showAlert("Error", "Failed to change password: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (nameField.getText().trim().isEmpty()) {
            errors.append("Name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        } else if (!isValidEmail(emailField.getText())) {
            errors.append("Invalid email format\n");
        }

        if (contactField.getText().trim().isEmpty()) {
            errors.append("Contact Number is required\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private boolean validatePasswordChange() {
        StringBuilder errors = new StringBuilder();

        if (currentPasswordField.getText().trim().isEmpty()) {
            errors.append("Current password is required\n");
        } else {
            try {
                if (!studentService.validatePassword(student.getId(), currentPasswordField.getText())) {
                    errors.append("Current password is incorrect\n");
                }
            } catch (SQLException e) {
                errors.append("Error validating password: " + e.getMessage() + "\n");
            }
        }

        if (newPasswordField.getText().trim().isEmpty()) {
            errors.append("New password is required\n");
        } else if (newPasswordField.getText().length() < 6) {
            errors.append("New password must be at least 6 characters long\n");
        }

        if (confirmPasswordField.getText().trim().isEmpty()) {
            errors.append("Please confirm your new password\n");
        } else if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
            errors.append("New passwords do not match\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void clearPasswordFields() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}