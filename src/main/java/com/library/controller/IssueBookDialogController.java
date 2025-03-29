package com.library.controller;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Student;
import com.library.model.Borrowing;
import com.library.service.BookService;
import com.library.service.StudentService;
import com.library.service.BorrowingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import java.time.LocalDate;
import java.util.List;

public class IssueBookDialogController {
    private final BookService bookService;
    private final StudentService studentService;
    private final BorrowingService borrowingService;
    private Book selectedBook;
    private ObservableList<Student> allStudents;
    private FilteredList<Student> filteredStudents;

    @FXML
    private Label bookTitleLabel;
    @FXML
    private TextField studentSearchField;
    @FXML
    private ListView<Student> studentListView;
    @FXML
    private DatePicker dueDatePicker;
    @FXML
    private Button issueButton;

    public IssueBookDialogController() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
        this.borrowingService = new BorrowingService();
    }

    public void setBook(Book book) {
        this.selectedBook = book;
        bookTitleLabel.setText("Issue: " + book.getTitle());
        loadStudents();
        setupStudentSearch();
    }

    private void setupStudentSearch() {
        studentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredStudents.setPredicate(student -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return student.getName().toLowerCase().contains(lowerCaseFilter) ||
                        student.getStudentId().toLowerCase().contains(lowerCaseFilter) ||
                        student.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
    }

    private void loadStudents() {
        // Load all students and create filtered list
        allStudents = FXCollections.observableArrayList(studentService.getAllStudents());
        filteredStudents = new FilteredList<>(allStudents, p -> true);
        studentListView.setItems(filteredStudents);
    }

    @FXML
    private void handleIssueBook() {
        if (!validateInput()) {
            return;
        }

        try {
            Student selectedStudent = studentListView.getSelectionModel().getSelectedItem();
            LocalDate dueDate = dueDatePicker.getValue();

            // Create borrowing record
            borrowingService.borrowBook(selectedStudent, selectedBook, dueDate);

            // Close dialog
            Stage stage = (Stage) issueButton.getScene().getWindow();
            stage.close();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to issue book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateInput() {
        if (studentListView.getSelectionModel().getSelectedItem() == null) {
            showAlert("Error", "Please select a student.", Alert.AlertType.ERROR);
            return false;
        }

        if (dueDatePicker.getValue() == null) {
            showAlert("Error", "Please select a due date.", Alert.AlertType.ERROR);
            return false;
        }

        if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
            showAlert("Error", "Due date cannot be in the past.", Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) bookTitleLabel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}