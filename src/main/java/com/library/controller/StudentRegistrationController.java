package com.library.controller;

import com.library.model.Student;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentRegistrationController {
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;

    private StudentService studentService = new StudentService();

    @FXML
    private void handleRegister() {
        // Validate inputs
        if (fullNameField.getText().trim().isEmpty() ||
                studentIdField.getText().trim().isEmpty() ||
                emailField.getText().trim().isEmpty() ||
                phoneField.getText().trim().isEmpty()) {

            showAlert("Error", "All fields are required!");
            return;
        }

        // Validate email format
        if (!emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Please enter a valid email address!");
            return;
        }

        try {
            // Register student
            Student student = studentService.registerStudent(
                    emailField.getText(),
                    fullNameField.getText(),
                    studentIdField.getText(),
                    phoneField.getText());

            showAlert("Success", "Registration successful! Student can login using:\nEmail: " +
                    student.getEmail() + "\nPassword: " + student.getStudentId());
            handleBackToLogin();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Registration failed: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

            Stage stage = (Stage) fullNameField.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Library Management System - Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load login screen: " + e.getMessage());
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