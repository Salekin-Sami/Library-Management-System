package com.library.controller;

import com.library.model.Book;
import com.library.model.BookRequest;
import com.library.model.Student;
import com.library.model.StudentProfile;
import com.library.service.BookService;
import com.library.service.StudentService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> searchFilter;
    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;

    @FXML
    private TableView<BookRequest> requestsTable;
    @FXML
    private TableColumn<BookRequest, String> requestBookTitleColumn;
    @FXML
    private TableColumn<BookRequest, String> requestDateColumn;
    @FXML
    private TableColumn<BookRequest, String> dueDateColumn;
    @FXML
    private TableColumn<BookRequest, String> requestStatusColumn;

    private StudentService studentService = new StudentService();
    private BookService bookService = new BookService();
    private Student currentStudent;
    private ObservableList<Book> books = FXCollections.observableArrayList();
    private ObservableList<BookRequest> requests = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize book table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().isAvailable() ? "Available" : "Not Available"));

        // Set default filter value
        searchFilter.setValue("All");

        // Initialize request table columns
        requestBookTitleColumn.setCellValueFactory(cellData -> {
            Book book = bookService.getBookById((long) cellData.getValue().getBookId());
            return new SimpleStringProperty(book != null ? book.getTitle() : "Unknown");
        });
        requestDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(cellData.getValue().getRequestDate().format(formatter));
        });
        dueDateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            return new SimpleStringProperty(cellData.getValue().getDueDate().format(formatter));
        });
        requestStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Set up double-click handler for books table
        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    handleBookRequest(selectedBook);
                }
            }
        });

        // Load initial data
        loadBooks();
        loadRequests();
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        welcomeLabel.setText("Welcome, " + student.getName());
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Book> searchResults;
            String filter = searchFilter.getValue();

            switch (filter) {
                case "Title":
                    searchResults = bookService.searchBooksByTitle(searchTerm);
                    break;
                case "Author":
                    searchResults = bookService.searchBooksByAuthor(searchTerm);
                    break;
                case "Category":
                    searchResults = bookService.searchBooksByCategory(searchTerm);
                    break;
                default:
                    searchResults = bookService.searchBooks(searchTerm);
            }

            // Filter to only show available books
            searchResults = searchResults.stream()
                    .filter(Book::isAvailable)
                    .toList();
            books.setAll(searchResults);
        } else {
            loadBooks();
        }
    }

    @FXML
    private void handleBookRequest(Book book) {
        if (!currentStudent.canBorrowMore()) {
            showAlert("Error", "You have reached the maximum number of books you can borrow!");
            return;
        }

        try {
            LocalDateTime dueDate = LocalDateTime.now().plusDays(14); // 2 weeks borrowing period
            boolean success = studentService.requestBook(currentStudent.getId(), book.getId(),
                    dueDate);

            if (success) {
                showAlert("Success", "Book request submitted successfully!");
                loadRequests();
            } else {
                showAlert("Error", "Failed to submit book request. Please try again.");
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to submit book request: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_profile.fxml"));
            Parent root = loader.load();
            StudentProfileController controller = loader.getController();

            // Convert Student to StudentProfile
            StudentProfile profile = new StudentProfile(
                    currentStudent.getId().intValue(),
                    currentStudent.getName(),
                    currentStudent.getStudentId(),
                    currentStudent.getContactNumber(),
                    currentStudent.getEmail(),
                    currentStudent.getBooksBorrowed(),
                    currentStudent.getMaxBooks());
            controller.setCurrentStudent(profile);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Student Profile");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load profile: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Library Management System - Login");
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Failed to load login screen: " + e.getMessage());
        }
    }

    private void loadBooks() {
        List<Book> availableBooks = bookService.getAvailableBooks();
        books.setAll(availableBooks);
        booksTable.setItems(books);
    }

    private void loadRequests() {
        List<BookRequest> studentRequests = studentService.getStudentRequests(currentStudent.getId());
        requests.setAll(studentRequests);
        requestsTable.setItems(requests);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}