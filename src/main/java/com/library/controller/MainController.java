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

public class MainController {
    private final BookService bookService;
    private final StudentService studentService;
    private final BorrowingService borrowingService;
    private final BookApiService bookApiService;
    private boolean isDarkTheme = false;

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

    public MainController() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
        this.borrowingService = new BorrowingService();
        this.bookApiService = new BookApiService();
    }

    @FXML
    public void initialize() {
        setupBookTable();
        setupStudentTable();
        setupBorrowingTable();
        loadInitialData();

        // Set initial theme
        root.getStyleClass().add("light-theme");

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
    }

    private void setupBookTable() {
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

        // Add double-click handler
        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    showBookDetails(selectedBook);
                }
            }
        });
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
        loadBooks();
        loadStudents();
        loadBorrowings();
        loadRecentActivities();
    }

    private void loadBooks() {
        List<Book> books = bookService.getAllBooks();
        booksTable.setItems(FXCollections.observableArrayList(books));
    }

    private void loadStudents() {
        List<Student> students = studentService.getAllStudents();
        studentsTable.setItems(FXCollections.observableArrayList(students));
    }

    private void loadBorrowings() {
        List<Borrowing> borrowings = borrowingService.getActiveBorrowings();
        borrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
    }

    private void loadRecentActivities() {
        // TODO: Implement recent activities loading
    }

    // Menu Item Handlers
    @FXML
    private void handleExit() {
        Platform.exit();
    }

    @FXML
    private void handleAddBook() {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/fxml/add_book_dialog.fxml"));

            if (loader.getLocation() == null) {
                throw new RuntimeException("Could not find add_book_dialog.fxml");
            }

            // Load the FXML and get the controller
            Parent root = loader.load();

            // Create and configure the dialog stage
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Book");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(true);
            dialogStage.setWidth(800);
            dialogStage.setHeight(600);

            // Show the dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the books table
            refreshBooksTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open add book dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewAllBooks() {
        loadBooks();
    }

    @FXML
    private void handleSearchBooks() {
        String searchTerm = bookSearchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Book> books = bookService.searchBooksByTitle(searchTerm);
            booksTable.setItems(FXCollections.observableArrayList(books));
        } else {
            // If search field is empty, show all books
            loadBooks();
        }
    }

    @FXML
    private void handleAddStudent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_student_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New Student");
            dialogStage.setScene(new Scene(root));

            dialogStage.showAndWait();

            // Refresh the students table
            loadStudents();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleViewAllStudents() {
        loadStudents();
    }

    @FXML
    private void handleSearchStudents() {
        String searchTerm = studentSearchField.getText().trim();
        if (!searchTerm.isEmpty()) {
            List<Student> students = studentService.searchStudentsByName(searchTerm);
            studentsTable.setItems(FXCollections.observableArrayList(students));
        } else {
            // If search field is empty, show all students
            loadStudents();
        }
    }

    @FXML
    private void handleAddCopy() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Error", "Please select a book to add a copy.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Add Copy");
        confirmDialog.setHeaderText("Add Copy of Book");
        confirmDialog.setContentText("Are you sure you want to add a copy of '" + selectedBook.getTitle() + "'?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookService.addCopy(selectedBook);
                    showAlert("Success", "New copy added successfully!", Alert.AlertType.INFORMATION);
                    refreshBooksTable();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to add copy: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void handleIssueBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("Error", "Please select a book to issue.");
            return;
        }

        if (!selectedBook.isAvailable()) {
            showAlert("Error", "This copy is not available for borrowing.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/issue_book_dialog.fxml"));
            Parent root = loader.load();
            IssueBookDialogController controller = loader.getController();
            controller.setBook(selectedBook);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Issue Book");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the tables
            loadBooks();
            loadBorrowings();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReturnBook() {
        Borrowing selectedBorrowing = borrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert("Error", "Please select a borrowing to return.");
            return;
        }

        if (selectedBorrowing.getReturnDate() != null) {
            showAlert("Error", "This book has already been returned.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/return_book_dialog.fxml"));
            Parent root = loader.load();
            ReturnBookDialogController controller = loader.getController();
            controller.setBorrowing(selectedBorrowing);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Return Book");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            // Refresh the tables
            loadBooks();
            loadBorrowings();
        } catch (Exception e) {
            e.printStackTrace();
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Delete Book: " + selectedBook.getTitle());
        alert.setContentText("Are you sure you want to delete this book?");

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                bookService.deleteBook(selectedBook);
                refreshBooksTable();
                showAlert("Success", "Book deleted successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to delete book: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleSearchBorrowings() {
        String searchTerm = borrowingSearchField.getText().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadBorrowings();
            return;
        }

        List<Borrowing> allBorrowings = borrowingService.getActiveBorrowings();
        List<Borrowing> filteredBorrowings = allBorrowings.stream()
                .filter(b -> b.getBookCopy().getBook().getTitle().toLowerCase().contains(searchTerm) ||
                        b.getStudent().getName().toLowerCase().contains(searchTerm))
                .toList();

        borrowingsTable.setItems(FXCollections.observableArrayList(filteredBorrowings));
    }

    @FXML
    private void handleOverdueBooks() {
        try {
            List<Borrowing> overdueList = borrowingService.getOverdueBorrowings();
            borrowingsTable.setItems(FXCollections.observableArrayList(overdueList));

            if (overdueList.isEmpty()) {
                showAlert("Information", "No overdue books found.", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to load overdue books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleFineCollection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/fine_report_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Fine Report");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));

            // Show the dialog and wait for it to close
            dialogStage.showAndWait();

            // Refresh the borrowings table after the fine report dialog is closed
            loadBorrowings();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open fine report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleViewBorrowingHistory() {
        try {
            refreshBorrowingsTable();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load borrowing history: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Book Selected", "Please select a book to edit.");
            return;
        }

        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Book");
            dialog.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_book_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            dialog.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> refreshBooksTable());
        } catch (IOException e) {
            showAlert("Error", "Could not open edit book dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("No Student Selected", "Please select a student to edit.");
            return;
        }

        try {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edit Student");
            dialog.initModality(Modality.APPLICATION_MODAL);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_student_dialog.fxml"));
            DialogPane dialogPane = loader.load();
            dialog.setDialogPane(dialogPane);

            dialog.showAndWait()
                    .filter(response -> response == ButtonType.OK)
                    .ifPresent(response -> refreshStudentsTable());
        } catch (IOException e) {
            showAlert("Error", "Could not open edit student dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteStudent() {
        Student selectedStudent = studentsTable.getSelectionModel().getSelectedItem();
        if (selectedStudent == null) {
            showAlert("Error", "Please select a student to delete.");
            return;
        }

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Student");
        confirmDialog
                .setContentText("Are you sure you want to delete student '" + selectedStudent.getName() + "'?\n\n" +
                        "This will:\n" +
                        "1. Delete all borrowing history for this student\n" +
                        "2. Remove the student from the system\n\n" +
                        "This action cannot be undone.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    studentService.deleteStudent(selectedStudent.getId());
                    showAlert("Success", "Student and their borrowing history deleted successfully!",
                            Alert.AlertType.INFORMATION);
                    refreshStudentsTable();
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert("Error", "Failed to delete student: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
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
    }

    private void refreshStudentsTable() {
        studentsTable.setItems(FXCollections.observableArrayList(studentService.getAllStudents()));
    }

    private void refreshBorrowingsTable() {
        borrowingsTable.setItems(FXCollections.observableArrayList(borrowingService.getAllBorrowings()));
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
}