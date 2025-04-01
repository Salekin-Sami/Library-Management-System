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
import java.time.LocalDateTime;
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
import com.library.model.BookRequest;
import com.library.model.RequestStatus;

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
    private TableView<BookRequest> requestsTable;
    @FXML
    private TableColumn<BookRequest, String> requestBookTitleColumn;
    @FXML
    private TableColumn<BookRequest, String> requestStudentNameColumn;
    @FXML
    private TableColumn<BookRequest, LocalDateTime> requestDateColumn;
    @FXML
    private TableColumn<BookRequest, LocalDateTime> requestDueDateColumn;
    @FXML
    private TableColumn<BookRequest, String> requestStatusColumn;
    @FXML
    private Button approveRequestButton;
    @FXML
    private Button rejectRequestButton;

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
    private Label dateLabel;

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

    public MainController() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
        this.borrowingService = new BorrowingService();
        this.bookApiService = new BookApiService();
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Called when the FXML file is loaded. This method is responsible for:
     * <ul>
     *     <li>Setting the initial theme</li>
     *     <li>Setting up tables</li>
     *     <li>Adding listeners to search fields</li>
     *     <li>Setting the current date</li>
     *     <li>Loading initial data</li>
     * </ul>
     */
/******  8bfe7999-77bd-4836-8835-6ba0a3747847  *******/
    @FXML
    public void initialize() {
        try {
            // Set initial theme
            root.getStyleClass().add("light-theme");

            // Setup tables
            setupBookTable();
            setupStudentTable();
            setupBorrowingTable();
            setupRequestsTable();

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

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Sets the current user and initializes the main view by setting up tables and loading data.
     *
     * @param user the current user
     */
/******  f8876ead-8775-478b-8ab3-f32a77f6d545  *******/
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
            setupRequestsTable();
            loadInitialData();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error in status bar if available
            if (welcomeLabel != null) {
                welcomeLabel.setText("Error initializing main view: " + e.getMessage());
            }
        }
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Sets up the book table by setting up its columns and adding double-click handler to show book details.
     *
     * The columns are:
     * <ul>
     *     <li>ID</li>
     *     <li>Title</li>
     *     <li>Author</li>
     *     <li>ISBN</li>
     *     <li>Category</li>
     *     <li>Status (Available/Not Available)</li>
     *     <li>Total Copies</li>
     * </ul>
     *
     * When a book is double-clicked, its details are shown in a dialog.
     */
/******  346f7db6-bf44-412e-9d1d-11320ae5cbde  *******/
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

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Sets up the student table by setting up its columns and adding double-click handler to show student details.
     *
     * The columns are:
     * <ul>
     *     <li>ID</li>
     *     <li>Name</li>
     *     <li>ID Number</li>
     *     <li>Email</li>
     *     <li>Contact</li>
     * </ul>
     *
     * When a student is double-clicked, their details are shown in a dialog.
     */
/******  add92c9b-6f59-4090-8df7-21801f77e38d  *******/
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

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Sets up the borrowing table by setting up its columns and adding double-click handler to show borrowing details.
     *
     * The columns are:
     * <ul>
     *     <li>ID</li>
     *     <li>Book</li>
     *     <li>Student</li>
     *     <li>Borrow Date</li>
     *     <li>Due Date</li>
     *     <li>Status</li>
     *     <li>Fine</li>
     * </ul>
     *
     * When a borrowing is double-clicked, its details are shown in a dialog.
     *
     * The fine amount column is added dynamically if it doesn't exist.
     *
     * The return button is enabled or disabled depending on the selection.
     */
/******  b2bb07ad-bea2-4d36-8df3-494535673265  *******/
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

    private void setupRequestsTable() {
        requestBookTitleColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook().getTitle()));
        requestStudentNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudent().getName()));
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        requestDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        requestStatusColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

        // Enable/disable approve/reject buttons based on selection
        approveRequestButton.setDisable(true);
        rejectRequestButton.setDisable(true);
        requestsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isEnabled = newSelection != null &&
                            newSelection.getStatus() == RequestStatus.PENDING;
                    approveRequestButton.setDisable(!isEnabled);
                    rejectRequestButton.setDisable(!isEnabled);
                });
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Loads initial data for the main view by calling individual load methods and
     * refreshing the requests table. If any of the individual load methods throw an
     * exception, an error alert is shown.
     */
/******  45385ff4-615d-47f4-a32e-65b28f59cb31  *******/
    private void loadInitialData() {
        try {
            loadBooks();
            loadStudents();
            loadBorrowings();
            loadRecentActivities();
            refreshRequestsTable();
            updateStatistics();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load initial data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Loads all books from the database and sets the items in the books table.
     * If an exception is thrown while loading the books, an error alert is shown.
     */
/******  1959e40b-231f-45e4-a42a-230615e6206c  *******/
    private void loadBooks() {
        try {
            List<Book> books = bookService.getAllBooks();
            booksTable.setItems(FXCollections.observableArrayList(books));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Loads all students from the database and sets the items in the students table.
     * If an exception is thrown while loading the students, an error alert is shown.
     */
/******  3c0f93e8-f732-435f-a1df-45033b7c4b85  *******/
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
            long totalBooks = bookService.getAllBooks().size();
            totalBooksLabel.setText(String.valueOf(totalBooks));

            // Update total students
            long totalStudents = studentService.getAllStudents().size();
            totalStudentsLabel.setText(String.valueOf(totalStudents));

            // Update current borrowings
            long currentBorrowings = borrowingService.getAllBorrowings().stream()
                    .filter(b -> b.getReturnDate() == null)
                    .count();
            currentBorrowingsLabel.setText(String.valueOf(currentBorrowings));

            // Update overdue books
            long overdueBooks = borrowingService.getAllBorrowings().stream()
                    .filter(b -> b.getReturnDate() == null && b.getDueDate().isBefore(LocalDate.now()))
                    .count();
            overdueBooksLabel.setText(String.valueOf(overdueBooks));

            // Update total unpaid fines
            double totalUnpaidFines = borrowingService.getAllBorrowings().stream()
                    .mapToDouble(b -> b.calculateFine())
                    .sum();
            totalFinesLabel.setText(String.format("$%.2f", totalUnpaidFines));
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

            // Get the current stage
            Stage currentStage = (Stage) root.getScene().getWindow();

            // Load login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent loginRoot = loader.load();

            // Create new scene with the login view
            Scene loginScene = new Scene(loginRoot);

            // Set the scene to the current stage
            currentStage.setScene(loginScene);
            currentStage.setTitle("Login - Library Management System");
            currentStage.show();
        } catch (Exception e) {
            showAlert("Error", "Failed to logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Handles the "Add Book" menu item. Loads the add book dialog and refreshes
     * the books table after adding a new book.
     */
/******  d1cb4a35-4926-45a7-92f3-a4f756372703  *******/
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

/*************  ✨ Codeium Command ⭐  *************/
/**
 * Handles the action of adding a new copy of the selected book.
 * If no book is selected from the table, a warning alert is shown.
 * On success, the books table is refreshed and a success alert is displayed.
 * If an error occurs while adding the copy, an error alert is shown.
 */

/******  b5b733a2-1517-4371-9510-24418ae1de5e  *******/
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

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Handles the action of searching for books by title, author, or subject.
     * If the search text is empty, all books are retrieved.
     * On success, the books table is refreshed with the search results.
     * If an error occurs while searching, an error alert is shown.
     */
/******  6bde81bc-abb7-4288-a353-95972ca53a49  *******/
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

/*************  ✨ Codeium Command ⭐  *************/
    /**
     * Handles the action of adding a new member.
     * A warning alert is shown until the functionality is implemented.
     */
/******  291bd0ac-0266-4c04-b866-155c0427768a  *******/
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

    /**
     * Handles the action of issuing a book to a member.
     * If no book is selected from the table, a warning alert is shown.
     * On success, the books table and borrowings table are refreshed.
     * If an error occurs while issuing the book, an error alert is shown.
     */
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

    /**
     * Handles the action of returning a borrowed book.
     * If no borrowing is selected from the table, a warning alert is shown.
     * On success, the borrowings table and books table are refreshed.
     * If an error occurs while returning the book, an error alert is shown.
     */
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

    /**
     * Handles the action of viewing overdue books.
     * Loads all borrowing records, which include overdue ones, into the borrowings table.
     */

    @FXML
    private void handleViewOverdueBooks() {
        loadBorrowings();
    }

    /**
     * Handles the action of generating a fine report.
     * Loads the fine report dialog with all active borrowing records.
     * On closing the dialog, the borrowings table is refreshed.
     */
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

    /**
     * Handles the action of viewing the borrowing history.
     * Loads the borrowing history dialog, which shows all borrowing records.
     * If an error occurs while loading the dialog, an error alert is shown.
     */
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

    /**
     * Handles the action of deleting a book.
     * If no book is selected from the table, a warning alert is shown.
     * On success, the books table is refreshed.
     * If an error occurs while deleting the book, an error alert is shown.
     */
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

    /**
     * Handles the action of searching for borrowings by book title, author, student name, or student ID.
     * If the search text is empty, all borrowings are retrieved.
     * On success, the borrowings table is refreshed with the search results.
     * If an error occurs while searching, an error alert is shown.
     */
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

/**
 * Handles the action of displaying overdue books.
 * Retrieves a list of overdue borrowings from the borrowing service
 * and updates the borrowings table with these records.
 * If an error occurs during this process, an error alert is shown.
 */

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

    /**
     * Handles the action of deleting a student.
     * If no student is selected from the table, a warning alert is shown.
     * On success, the students table is refreshed.
     * If an error occurs while deleting the student, an error alert is shown.
     */
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

    /**
     * Handles the action of adding a new student.
     * Loads the add student dialog and refreshes the students table after adding a new student.
     * If an error occurs while loading the dialog, an error alert is shown.
     */
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

    /**
     * Handles the action of adding 20 classic books to the library.
     * If an error occurs while adding the books, an error alert is shown.
     * On success, the books table is refreshed.
     */
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

/**
 * Toggles the application's theme between light and dark modes.
 * Updates the root style class list to apply the appropriate theme.
 */

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

    /**
     * Confirms with the user whether they want to exit the application.
     * If the user selects OK, the application is terminated.
     */
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

    /**
     * Shows an information dialog with a user guide for the Library Management System.
     * The guide explains the features of the system for students and administrators.
     */
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

    /**
     * Shows an information dialog with version and feature information about the Library Management System.
     */
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

    /**
     * Handles the action of viewing the borrowing history of a selected student.
     * Loads the student history dialog, which shows all borrowing records of the student.
     * If an error occurs while loading the dialog, an error alert is shown.
     */
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

    /**
     * Handles the action of viewing the account information of the current student.
     * Loads the account dialog with the current student's information.
     * If an error occurs while loading the dialog, an error alert is shown.
     * After the dialog is closed, the students table is refreshed.
     */
    @FXML
    private void handleMyAccount() {
        try {
            // Get current student
            Student currentStudent = studentService.getStudentByEmail(currentUser.getEmail());
            if (currentStudent == null) {
                showAlert("Error", "Student information not found.", Alert.AlertType.ERROR);
                return;
            }

            // Load account dialog
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account_dialog.fxml"));
            Parent root = loader.load();
            AccountDialogController controller = loader.getController();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("My Account");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.setResizable(false);

            controller.setStudent(currentStudent);
            controller.setDialogStage(dialogStage);
            dialogStage.showAndWait();

            // Refresh tables after any changes
            refreshStudentsTable();
        } catch (Exception e) {
            showAlert("Error", "Failed to open account dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

/**
 * Handles the action of approving a book request.
 * If no request is selected from the table, a warning alert is shown.
 * On success, a new borrowing record is created, and the request status is updated to approved.
 * The requests, borrowings, and books tables are refreshed.
 * If an error occurs while approving the request, an error alert is shown.
 */

    @FXML
    private void handleApproveRequest() {
        BookRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("No Selection", "Please select a request to approve.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Create a new borrowing from the request
            Borrowing borrowing = new Borrowing();
            borrowing.setStudent(selectedRequest.getStudent());
            borrowing.setBookCopy(selectedRequest.getBook().getCopies().get(0)); // Get first available copy
            borrowing.setBorrowDate(LocalDate.now());
            borrowing.setDueDate(selectedRequest.getDueDate().toLocalDate());
            borrowing.setReturnDate(null); // Set return date to null for new borrowings

            // Update request status
            selectedRequest.setStatus(RequestStatus.APPROVED);

            // Save changes
            borrowingService.addBorrowing(borrowing);
            studentService.updateRequestStatus(selectedRequest.getId(), RequestStatus.APPROVED);

            // Refresh tables
            refreshRequestsTable();
            refreshBorrowingsTable();
            refreshBooksTable();

            showAlert("Success", "Book request approved successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to approve request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * Handles the action of rejecting a book request.
     * If no request is selected from the table, a warning alert is shown.
     * On success, the request status is updated to rejected, and the requests table is refreshed.
     * If an error occurs while rejecting the request, an error alert is shown.
     */
    @FXML
    private void handleRejectRequest() {
        BookRequest selectedRequest = requestsTable.getSelectionModel().getSelectedItem();
        if (selectedRequest == null) {
            showAlert("No Selection", "Please select a request to reject.", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Update request status
            selectedRequest.setStatus(RequestStatus.REJECTED);
            studentService.updateRequestStatus(selectedRequest.getId(), RequestStatus.REJECTED);

            // Refresh requests table
            refreshRequestsTable();

            showAlert("Success", "Book request rejected successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to reject request: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

/**
 * Refreshes the requests table by retrieving all pending book requests from the student service
 * and updating the table with the retrieved data. If an error occurs during retrieval, an error
 * alert is displayed.
 */

    private void refreshRequestsTable() {
        try {
            List<BookRequest> requests = studentService.getAllPendingRequests();
            requestsTable.setItems(FXCollections.observableArrayList(requests));
        } catch (Exception e) {
            showAlert("Error", "Failed to refresh requests table: " + e.getMessage(), Alert.AlertType.ERROR);
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

    /**
     * Opens a dialog to display the details of a book.
     * If no book is passed in, the dialog is not opened.
     * If an exception occurs while loading the dialog, an error alert is shown.
     * After the dialog is closed, the books table is refreshed.
     * @param book the book whose details to show
     */
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

    /**
     * Opens a dialog to display the details of a student.
     * If no student is passed in, the dialog is not opened.
     * If an exception occurs while loading the dialog, an error alert is shown.
     * After the dialog is closed, the students table and borrowings table are refreshed.
     * @param student the student whose details to show
     */
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

    /**
     * Handles the action of searching for students by name, student ID, or email.
     * If the search text is empty, all students are retrieved.
     * On success, the students table is refreshed with the search results.
     * If an error occurs while searching, an error alert is shown.
     */
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