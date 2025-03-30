package com.library.controller;

import com.library.model.Book;
import com.library.model.Student;
import com.library.model.Borrowing;
import com.library.service.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.time.LocalDate;
import java.util.List;
import javafx.stage.Modality;
import javafx.scene.Scene;
import java.io.IOException;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.VBox;
import com.library.model.User;
import java.util.prefs.Preferences;
import java.time.format.DateTimeFormatter;

public class MainController {
    private final BookService bookService;
    private final StudentService studentService;
    private final BorrowingService borrowingService;
    private final BookApiService bookApiService;
    private boolean isDarkTheme = false;
    private User currentUser;

    @FXML
    private TableView<Book> booksTable;
    @FXML
    private TableColumn<Book, Long> idColumn;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> categoryColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;
    @FXML
    private TableColumn<Book, Integer> totalCopiesColumn;

    @FXML
    private TableView<Student> studentsTable;
    @FXML
    private TableColumn<Student, Long> studentIdColumn;
    @FXML
    private TableColumn<Student, String> studentNameColumn;
    @FXML
    private TableColumn<Student, String> studentIdNumberColumn;
    @FXML
    private TableColumn<Student, String> studentEmailColumn;
    @FXML
    private TableColumn<Student, String> studentContactColumn;

    @FXML
    private TableView<Borrowing> borrowingsTable;
    @FXML
    private TableColumn<Borrowing, Long> borrowingIdColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingBookColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingStudentColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowingDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowingDueDateColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingStatusColumn;
    @FXML
    private Button returnBookButton;

    @FXML
    private TextField bookSearchField;
    @FXML
    private TextField studentSearchField;
    @FXML
    private ListView<String> recentActivitiesList;

    @FXML
    private TextField borrowingSearchField;

    @FXML
    private VBox root;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label totalBooksLabel;
    @FXML
    private Label totalStudentsLabel;
    @FXML
    private Label currentBorrowingsLabel;
    @FXML
    private Label overdueBooksLabel;
    @FXML
    private Label totalFinesLabel;

    @FXML
    private Label dateLabel;

    public MainController() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
        this.borrowingService = new BorrowingService();
        this.bookApiService = new BookApiService();
    }

    @FXML
    public void initialize() {
        try {
            // Set initial theme
            root.getStyleClass().add("light-theme");

            // Setup tables
            setupBookTable();
            setupStudentTable();
            setupBorrowingTable();

            // Add listeners to search fields
            bookSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadBooks();
                }
            });

            studentSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadStudents();
                }
            });

            borrowingSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadBorrowings();
                }
            });

            // Set current date
            if (dateLabel != null) {
                dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            }

            // Load initial data
            loadInitialData();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to initialize main view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setUser(User user) {
        try {
            this.currentUser = user;
            if (welcomeLabel != null) {
                welcomeLabel.setText("Welcome, " + user.getEmail() + " (" + user.getRole() + ")");
            }

            // Setup tables and load data after user is set
            setupBookTable();
            setupStudentTable();
            setupBorrowingTable();
            loadInitialData();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error in status bar if available
            if (welcomeLabel != null) {
                welcomeLabel.setText("Error initializing main view: " + e.getMessage());
            }
        }
    }

    private void setupBookTable() {
        try {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
            authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
            isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
            statusColumn.setCellValueFactory(cellData -> {
                Book book = cellData.getValue();
                long availableCopies = book.getCopies().stream()
                        .filter(copy -> "Available".equals(copy.getStatus()))
                        .count();
                return new SimpleStringProperty(availableCopies > 0 ? "Available" : "Not Available");
            });
            totalCopiesColumn.setCellValueFactory(
                    cellData -> new SimpleIntegerProperty(cellData.getValue().getCopies().size()).asObject());

            // Add double-click handler for book details
            booksTable.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                    if (selectedBook != null) {
                        showBookDetails(selectedBook);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            if (welcomeLabel != null) {
                welcomeLabel.setText("Error setting up book table: " + e.getMessage());
            }
        }
    }

    private void setupStudentTable() {
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        studentIdNumberColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        studentEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        studentContactColumn.setCellValueFactory(new PropertyValueFactory<>("contactNumber"));

        // Add double-click handler
        studentsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
                if (selectedStudent != null) {
                    showStudentDetails(selectedStudent);
                }
            }
        });
    }

    private void setupBorrowingTable() {
        borrowingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        borrowingBookColumn
                .setCellValueFactory(
                        cellData -> new SimpleStringProperty(cellData.getValue().getBookCopy().getBook().getTitle()));
        borrowingStudentColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudent().getName()));
        borrowingDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        borrowingDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        borrowingStatusColumn.setCellValueFactory(cellData -> {
            Borrowing borrowing = cellData.getValue();
            String status = borrowing.getReturnDate() != null ? "Returned" : "Borrowed";
            if (borrowing.getReturnDate() == null && borrowing.getDueDate().isBefore(LocalDate.now())) {
                status = "Overdue";
            }
            return new SimpleStringProperty(status);
        });

        // Add fine amount column if it doesn't exist
        if (borrowingsTable.getColumns().stream()
                .noneMatch(col -> col.getId() != null && col.getId().equals("fineColumn"))) {
            TableColumn<Borrowing, String> fineColumn = new TableColumn<>("Fine");
            fineColumn.setId("fineColumn");
            fineColumn.setCellValueFactory(cellData -> {
                double fineAmount = cellData.getValue().calculateFine();
                return new SimpleStringProperty(fineAmount > 0
                        ? String.format("$%.2f%s", fineAmount, cellData.getValue().isFinePaid() ? " (Paid)" : "")
                        : "");
            });
            borrowingsTable.getColumns().add(fineColumn);
        }

        // Enable/disable return button based on selection
        returnBookButton.setDisable(true);
        borrowingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isEnabled = newSelection != null && newSelection.getReturnDate() == null;
                    returnBookButton.setDisable(!isEnabled);
                });
    }

    private void loadInitialData() {
        try {
            loadBooks();
            loadStudents();
            loadBorrowings();
            loadRecentActivities();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load initial data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            booksTable.setItems(FXCollections.observableArrayList(books));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentsTable.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadBorrowings() {
        try {
            List<Borrowing> borrowings = borrowingService.getAllBorrowings();
            borrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load borrowings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRecentActivities() {
        // TODO: Implement recent activities loading
    }

    private void updateStatistics() {
        try {
            // Update total books
            totalBooksLabel.setText(String.valueOf(bookService.getAllBooks().size()));

            // Update total students
            totalStudentsLabel.setText(String.valueOf(studentService.getAllStudents().size()));

            // Update current borrowings
            List<Borrowing> currentBorrowings = borrowingService.getCurrentBorrowings();
            currentBorrowingsLabel.setText(String.valueOf(currentBorrowings.size()));

            // Update overdue books
            List<Borrowing> overdueBorrowings = borrowingService.getOverdueBorrowings();
            overdueBooksLabel.setText(String.valueOf(overdueBorrowings.size()));

            // Update total unpaid fines
            double totalFines = borrowingService.getTotalUnpaidFines();
            totalFinesLabel.setText(String.format("$%.2f", totalFines));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to update statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // Menu Item Handlers
    @FXML
    private void handleLogout() {
        try {
            // Clear any saved credentials
            Preferences prefs = Preferences.userRoot().node("com.library.login");
            prefs.remove("email");
            prefs.remove("password");
            prefs.putBoolean("remember", false);

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) root.getScene().getWindow();
            stage.setTitle("Login - Library Management System");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddBook() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_book_dialog.fxml"));
            Parent root = loader.load();
            AddBookDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Book");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the books table after adding a new book
            refreshBooksTable();
        } catch (IOException e) {
            showAlert("Error", "Could not open add book dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddCopy() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to add a copy.", Alert.AlertType.WARNING);
            return;
        }

        try {
            bookService.addCopy(selectedBook);
            refreshBooksTable();
            showAlert("Success", "New copy added successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to add book copy: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearchBooks() {
        String searchText = bookSearchField.getText().trim();
        try {
            List<Book> books;
            if (searchText.isEmpty()) {
                books = bookService.getAllBooks();
            } else {
                books = bookService.searchBooks(searchText);
            }
            booksTable.setItems(FXCollections.observableArrayList(books));
        } catch (Exception e) {
            showAlert("Error", "Failed to search books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddMember() {
        // TODO: Implement add member functionality
        showAlert("Info", "Add Member functionality coming soon!");
    }

    @FXML
    private void handleSearchMembers() {
        // TODO: Implement search members functionality
        showAlert("Info", "Search Members functionality coming soon!");
    }

    @FXML
    private void handleIssueBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to issue.", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/issue_book_dialog.fxml"));
            Parent root = loader.load();
            IssueBookDialogController controller = loader.getController();
            controller.setBook(selectedBook);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Issue Book");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh tables after issuing the book
            refreshBooksTable();
            refreshBorrowingsTable();
        } catch (IOException e) {
            showAlert("Error", "Could not open issue book dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleReturnBook() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("No Selection", "Please select a borrowing to return.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Calculate fine if overdue
            double fineAmount = selectedBorrowing.calculateFine();

            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Return Book");
            confirmDialog.setHeaderText("Confirm Return");

            if (fineAmount > 0) {
                confirmDialog.setContentText(String.format(
                        "This book is overdue. Fine amount: $%.2f\nDo you want to proceed with the return?",
                        fineAmount));
            } else {
                confirmDialog.setContentText("Are you sure you want to return this book?");
            }

            confirmDialog.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        borrowingService.returnBook(selectedBorrowing.getId());
                        refreshBorrowingsTable();
                        refreshBooksTable();
                        showAlert("Success", "Book returned successfully!", Alert.AlertType.INFORMATION);
                    } catch (Exception e) {
                        showAlert("Error", "Failed to return book: " + e.getMessage(), Alert.AlertType.ERROR);
                    }
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to process return: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewOverdueBooks() {
        loadBorrowings();
    }

    @FXML
    private void handleFineReport() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/fine_report_dialog.fxml"));
            Parent root = loader.load();
            FineReportDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Fine Report");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setWidth(800);
            dialogStage.setHeight(600);

            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            // Refresh the borrowings table after the dialog is closed
            loadBorrowings();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open fine report dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBorrowingHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/borrowing_history_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Borrowing History");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Book");
        confirmDialog.setHeaderText("Confirm Delete");
        confirmDialog.setContentText("Are you sure you want to delete this book?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookService.deleteBook(selectedBook);
                    refreshBooksTable();
                    showAlert("Success", "Book deleted successfully!", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete book: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleSearchBorrowings() {
        String searchText = borrowingSearchField.getText().trim();
        try {
            List<Borrowing> borrowings;
            if (searchText.isEmpty()) {
                borrowings = borrowingService.getAllBorrowings();
            } else {
                borrowings = borrowingService.searchBorrowings(searchText);
            }
            borrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
        } catch (Exception e) {
            showAlert("Error", "Failed to search borrowings: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleOverdueBooks() {
        try {
            List<Borrowing> overdueBorrowings = borrowingService.getOverdueBorrowings();
            borrowingsTable.setItems(FXCollections.observableArrayList(overdueBorrowings));
        } catch (Exception e) {
            showAlert("Error", "Failed to load overdue books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFineCollection() {
        try {
            List<Borrowing> fineBorrowings = borrowingService.getFineBorrowings();
            borrowingsTable.setItems(FXCollections.observableArrayList(fineBorrowings));
        } catch (Exception e) {
            showAlert("Error", "Failed to load fine collection: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewBorrowingHistory() {
        try {
            List<Borrowing> borrowings = borrowingService.getAllBorrowings();
            borrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
        } catch (Exception e) {
            showAlert("Error", "Failed to load borrowing history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to edit.", Alert.AlertType.WARNING);
            return;
        }
        // TODO: Implement book editing dialog
        showAlert("Info", "Edit Book functionality coming soon!");
    }

    @FXML
    private void handleEditStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to edit.", Alert.AlertType.WARNING);
            return;
        }
        // TODO: Implement student editing dialog
        showAlert("Info", "Edit Student functionality coming soon!");
    }

    @FXML
    private void handleDeleteStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Selection", "Please select a student to delete.", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Student");
        confirmDialog.setHeaderText("Confirm Delete");
        confirmDialog.setContentText("Are you sure you want to delete this student?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
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
        });
    }

    @FXML
    private void handleAddStudent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_student_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Student");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the students table after adding a new student
            refreshStudentsTable();
        } catch (IOException e) {
            showAlert("Error", "Could not open add student dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddClassicBooks() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Add Classic Books");
        confirmDialog.setHeaderText("Add 20 Classic Books");
        confirmDialog.setContentText("Are you sure you want to add 20 classic books to the library?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookService.addClassicBooks();
                    showAlert("Success", "20 classic books have been added to the library!",
                            Alert.AlertType.INFORMATION);
                    refreshBooksTable();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to add classic books: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            root.getStyleClass().remove("light-theme");
            root.getStyleClass().add("dark-theme");
        } else {
            root.getStyleClass().remove("dark-theme");
            root.getStyleClass().add("light-theme");
        }
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Are you sure you want to exit the application?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Platform.exit();
            }
        });
    }

    @FXML
    private void handleUserGuide() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("User Guide");
        alert.setHeaderText("Library Management System - User Guide");
        alert.setContentText(
                "Welcome to the Library Management System!\n\n" +
                        "For Students:\n" +
                        "- Login using your email and student ID as password\n" +
                        "- Browse available books\n" +
                        "- View your borrowing history\n" +
                        "- Check due dates and fines\n\n" +
                        "For Administrators:\n" +
                        "- Manage books (add, edit, delete)\n" +
                        "- Register new students\n" +
                        "- Handle book borrowings and returns\n" +
                        "- Generate reports\n\n" +
                        "For assistance, please contact the library staff.");
        alert.showAndWait();
    }

    @FXML
    private void handleAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About");
        alert.setHeaderText("Library Management System");
        alert.setContentText(
                "Version: 1.0\n" +
                        "A comprehensive system for managing library operations.\n\n" +
                        "Features:\n" +
                        "- Book Management\n" +
                        "- Student Management\n" +
                        "- Borrowing Management\n" +
                        "- Fine Calculation\n" +
                        "- Reports Generation");
        alert.showAndWait();
    }

    @FXML
    private void handleViewStudentHistory() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("Error", "Please select a student first!", Alert.AlertType.WARNING);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/student_history.fxml"));
            Parent root = loader.load();

            StudentHistoryController controller = loader.getController();
            controller.setStudent(selectedStudent);

            Stage stage = new Stage();
            stage.setTitle("Student History - " + selectedStudent.getName());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load student history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void refreshBooksTable() {
        booksTable.setItems(FXCollections.observableArrayList(bookService.getAllBooks()));
        updateStatistics();
    }

    private void refreshStudentsTable() {
        studentsTable.setItems(FXCollections.observableArrayList(studentService.getAllStudents()));
        updateStatistics();
    }

    private void refreshBorrowingsTable() {
        borrowingsTable.setItems(FXCollections.observableArrayList(borrowingService.getAllBorrowings()));
        updateStatistics();
    }

    private void showBookDetails(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_details_dialog.fxml"));
            Parent root = loader.load();
            BookDetailsDialogController controller = loader.getController();
            controller.setBook(book, true); // true for librarian view

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Details: " + book.getTitle());
            dialogStage.setScene(new Scene(root, 800, 600));
            dialogStage.showAndWait();

            // Refresh the books table after dialog is closed
            refreshBooksTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open book details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showStudentDetails(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/student_details_dialog.fxml"));
            Parent root = loader.load();

            StudentDetailsDialogController controller = loader.getController();
            controller.setStudent(student);

            Stage stage = new Stage();
            stage.setTitle("Student Details - " + student.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.setWidth(800);
            stage.setHeight(600);
            stage.showAndWait();

            // Refresh tables after any changes
            refreshStudentsTable();
            refreshBorrowingsTable();
        } catch (IOException e) {
            showAlert("Error", "Failed to open student details: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearchStudents() {
        String searchText = studentSearchField.getText().trim();
        try {
            List<Student> students;
            if (searchText.isEmpty()) {
                students = studentService.getAllStudents();
            } else {
                students = studentService.searchStudents(searchText);
            }
            studentsTable.setItems(FXCollections.observableArrayList(students));
        } catch (Exception e) {
            showAlert("Error", "Failed to search students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
}