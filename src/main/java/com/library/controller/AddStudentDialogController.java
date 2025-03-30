package com.library.controller;

import com.library.model.Student;
import com.library.model.UserRole;
import com.library.service.StudentService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

public class AddStudentDialogController {
    private final StudentService studentService;
    private final Random random;

    // Lists for random name generation
    private final List<String> firstNames = Arrays.asList(
            "James", "John", "Robert", "Michael", "William", "David", "Richard", "Joseph", "Thomas", "Charles",
            "Mary", "Patricia", "Jennifer", "Linda", "Elizabeth", "Barbara", "Susan", "Jessica", "Sarah", "Karen");

    private final List<String> lastNames = Arrays.asList(
            "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
            "Anderson", "Taylor", "Thomas", "Moore", "Jackson", "Martin", "Lee", "Thompson", "White", "Harris");

    @FXML
    private TextField studentIdField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField contactNumberField;
    @FXML
    private ComboBox<UserRole> roleComboBox;

    private Student editingStudent;

    public AddStudentDialogController() {
        this.studentService = new StudentService();
        this.random = new Random();
    }

    @FXML
    public void initialize() {
        // Remove role selection since it will always be student
        roleComboBox.setVisible(false);
        roleComboBox.setManaged(false);
    }

    @FXML
    private void handleGenerateRandom() {
        // Generate random name
        String firstName = firstNames.get(random.nextInt(firstNames.size()));
        String lastName = lastNames.get(random.nextInt(lastNames.size()));
        String fullName = firstName + " " + lastName;

        // Generate random student ID (Year + Department Code + 4 digit number)
        int currentYear = LocalDate.now().getYear() % 100;
        String deptCode = String.format("%02d", random.nextInt(10));
        String serialNum = String.format("%04d", random.nextInt(10000));
        String studentId = currentYear + deptCode + serialNum;

        // Generate email based on name
        String email = (firstName.toLowerCase() + "." + lastName.toLowerCase() + "@university.edu").replace(" ", "");

        // Generate random phone number
        String phoneNumber = String.format("+1-%03d-%03d-%04d",
                random.nextInt(800) + 200, // Area code between 200-999
                random.nextInt(1000), // First 3 digits
                random.nextInt(10000) // Last 4 digits
        );

        // Set the generated values
        nameField.setText(fullName);
        studentIdField.setText(studentId);
        emailField.setText(email);
        contactNumberField.setText(phoneNumber);
    }

    @FXML
    private void handleAddStudent() {
        if (validateInput()) {
            try {
                // Register student using the updated method
                Student student = studentService.registerStudent(
                        emailField.getText(),
                        nameField.getText(),
                        studentIdField.getText(),
                        contactNumberField.getText());

                if (student != null) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText("Student Registered Successfully");
                    alert.setContentText(
                            "Email: " + student.getEmail() + "\nStudent ID (Password): " + student.getStudentId());
                    alert.showAndWait();
                }

                // Close the dialog
                Stage stage = (Stage) studentIdField.getScene().getWindow();
                stage.close();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Registration Failed");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) studentIdField.getScene().getWindow();
        stage.close();
    }

    public void setEditMode(Student student) {
        this.editingStudent = student;

        // Populate fields with student data
        studentIdField.setText(student.getStudentId());
        nameField.setText(student.getName());
        emailField.setText(student.getEmail());
        contactNumberField.setText(student.getContactNumber());
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();

        if (studentIdField.getText().trim().isEmpty()) {
            errors.append("Student ID is required\n");
        } else if (editingStudent == null) {
            // Only check for duplicate ID when adding new student
            Student existingStudent = studentService.getStudentByStudentId(studentIdField.getText());
            if (existingStudent != null) {
                errors.append("Student ID already exists\n");
            }
        }

        if (nameField.getText().trim().isEmpty()) {
            errors.append("Name is required\n");
        }

        if (emailField.getText().trim().isEmpty()) {
            errors.append("Email is required\n");
        } else if (!isValidEmail(emailField.getText())) {
            errors.append("Invalid email format\n");
        }

        if (contactNumberField.getText().trim().isEmpty()) {
            errors.append("Contact Number is required\n");
        }

        if (errors.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText("Please correct the following errors:");
            alert.setContentText(errors.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
}