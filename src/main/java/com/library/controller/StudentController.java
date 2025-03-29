package com.library.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import com.library.model.Student;
import com.library.service.StudentService;
import javafx.collections.FXCollections;
import java.util.List;

public class StudentController {

    @FXML
    private TableView<Student> studentsTable;

    @FXML
    private TableColumn<Student, Integer> idColumn;

    @FXML
    private TableColumn<Student, String> nameColumn;

    @FXML
    private TableColumn<Student, String> emailColumn;

    private StudentService studentService;

    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @FXML
    private void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<Student, Integer>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<Student, String>("email"));
    }

    @FXML
    private void handleDelete() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to delete.", Alert.AlertType.WARNING);
            return;
        }

        try {
            studentService.deleteStudent(selectedStudent.getId());
            refreshStudentsTable();
            showAlert("Success", "Student deleted successfully!", Alert.AlertType.INFORMATION);
        } catch (IllegalStateException e) {
            showAlert("Cannot Delete", e.getMessage(), Alert.AlertType.WARNING);
        } catch (Exception e) {
            showAlert("Error", "Failed to delete student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshStudentsTable() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentsTable.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh students table: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}