package com.library.controller;

import com.library.model.Student;
import com.library.model.StudentProfile;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class StudentProfileController {
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private Label booksBorrowedLabel;
    @FXML
    private Label maxBooksLabel;

    private StudentService studentService = new StudentService();
    private Student currentStudent;
    private StudentProfile currentProfile;

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        fullNameLabel.setText(student.getName());
        studentIdLabel.setText(student.getStudentId());
        emailField.setText(student.getEmail());
        phoneField.setText(student.getContactNumber());
        booksBorrowedLabel.setText(String.valueOf(student.getBooksBorrowed()));
        maxBooksLabel.setText(String.valueOf(student.getMaxBooks()));
    }

    public void setCurrentStudent(StudentProfile profile) {
        this.currentProfile = profile;
        fullNameLabel.setText(profile.getFullName());
        studentIdLabel.setText(profile.getStudentId());
        emailField.setText(profile.getEmail());
        phoneField.setText(profile.getPhoneNumber());
        booksBorrowedLabel.setText(String.valueOf(profile.getBooksBorrowed()));
        maxBooksLabel.setText(String.valueOf(profile.getMaxBooks()));
    }

    @FXML
    private void handleUpdateProfile() {
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Error", "Please enter a valid email address!");
            return;
        }

        // Validate phone number (basic validation)
        if (!phone.matches("^\\d{10}$")) {
            showAlert("Error", "Please enter a valid 10-digit phone number!");
            return;
        }

        Long userId = currentStudent != null ? currentStudent.getId() : (long) currentProfile.getUserId();
        if (studentService.updateStudentProfile(userId, phone, email)) {
            showAlert("Success", "Profile updated successfully!");
            if (currentStudent != null) {
                currentStudent.setEmail(email);
                currentStudent.setContactNumber(phone);
            } else {
                currentProfile.setEmail(email);
                currentProfile.setPhoneNumber(phone);
            }
        } else {
            showAlert("Error", "Failed to update profile. Please try again.");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_dashboard.fxml"));
            Parent root = loader.load();
            StudentDashboardController controller = loader.getController();
            if (currentStudent != null) {
                controller.setCurrentStudent(currentStudent);
            } else {
                // Convert StudentProfile to Student
                Student student = studentService.getStudentById((long) currentProfile.getUserId());
                controller.setCurrentStudent(student);
            }

            Stage stage = (Stage) fullNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Student Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard screen.");
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