package com.library.controller;

import com.library.model.Book;
import com.library.model.BookRequest;
import com.library.model.Student;
import com.library.model.StudentProfile;
import com.library.model.RequestStatus;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import com.library.model.Borrowing;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.layout.VBox;
import com.library.model.User;
import java.util.prefs.Preferences;
import java.time.format.DateTimeFormatter;
import com.library.service.BorrowingService;
import java.io.IOException;
import com.library.chat.ChatMessage;
import com.library.chat.ChatService;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.AnchorPane;

public class StudentDashboardController {
    @FXML
    private Label welcomeLabel;
    @FXML
    private TextField bookSearchField;
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
    private TableColumn<Book, Integer> availableColumn;
    @FXML
    private Button requestBookButton;

    @FXML
    private TableView<BookRequest> requestsTable;
    @FXML
    private TableColumn<BookRequest, String> requestBookTitleColumn;
    @FXML
    private TableColumn<BookRequest, LocalDateTime> requestDateColumn;
    @FXML
    private TableColumn<BookRequest, LocalDateTime> requestDueDateColumn;
    @FXML
    private TableColumn<BookRequest, String> requestStatusColumn;

    @FXML
    private TableView<Borrowing> borrowingsTable;
    @FXML
    private TableColumn<Borrowing, String> borrowingBookTitleColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowingDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowingDueDateColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingStatusColumn;

    @FXML
    private AnchorPane clientChatPane;
    @FXML
    private ListView<ChatMessage> chatListView;
    @FXML
    private TextField chatInputField;
    @FXML
    private AnchorPane chatView;

    @FXML
    private TableView<Book> recommendedBooksTable;
    @FXML
    private TableColumn<Book, String> recommendedTitleColumn;
    @FXML
    private TableColumn<Book, String> recommendedAuthorColumn;
    @FXML
    private TableColumn<Book, String> recommendedStatusColumn;

    private final BookService bookService;
    private final StudentService studentService;
    private final BorrowingService borrowingService;
    private Student currentStudent;
    private User currentUser;
    private ObservableList<Book> books = FXCollections.observableArrayList();
    private ObservableList<BookRequest> requests = FXCollections.observableArrayList();

    public StudentDashboardController() {
        this.bookService = new BookService();
        this.studentService = new StudentService();
        this.borrowingService = new BorrowingService();
    }

    /**
     * Initializes the StudentDashboardController by setting up the tables, adding
     * listeners to the search fields, and loading the initial data.
     */
    @FXML
    public void initialize() {
        try {
            // Setup tables
            setupBooksTable();
            setupRequestsTable();
            setupBorrowingsTable();
            setupRecommendedBooksTable(); // NEW: setup recommendations table

            // Add listeners to search fields
            // Clear search results when search field is empty
            bookSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue.isEmpty()) {
                    loadBooks();
                }
            });

            // Load only books initially
            loadBooks();
            loadRecommendedBooks(); // NEW: load recommendations

            // Chat setup
            if (chatListView != null) {
                // Always use currentStudent.getEmail() if available
                chatListView.setItems(
                        ChatService.getMessagesForRecipient(currentStudent != null ? currentStudent.getEmail() : null));
                ChatService.getMessages().addListener((ListChangeListener<ChatMessage>) c -> {
                    chatListView.setItems(ChatService
                            .getMessagesForRecipient(currentStudent != null ? currentStudent.getEmail() : null));
                    chatListView.scrollTo(chatListView.getItems().size() - 1);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to initialize student dashboard: " + e.getMessage());
        }
    }

    /**
     * Sets the current user and loads the associated student data.
     * If the student is found, the welcome label is updated with the student's
     * name.
     * If the student is not found, an error alert is shown.
     * 
     * @param user The current user.
     */
    public void setUser(User user) {
        this.currentUser = user;
        this.currentStudent = studentService.getStudentByEmail(user.getEmail());
        if (currentStudent != null) {
            welcomeLabel.setText("Welcome, " + currentStudent.getName());
            loadInitialData();
            loadRecommendedBooks(); // NEW
            // Update chatListView for the correct student after login
            if (chatListView != null) {
                chatListView.setItems(ChatService.getMessagesForRecipient(currentStudent.getEmail()));
                ChatService.getMessages().addListener((ListChangeListener<ChatMessage>) c -> {
                    chatListView.setItems(ChatService.getMessagesForRecipient(currentStudent.getEmail()));
                    chatListView.scrollTo(chatListView.getItems().size() - 1);
                });
            }
        } else {
            showAlert("Error", "Student information not found.");
        }
    }

    public void setCurrentStudent(Student student) {
        this.currentStudent = student;
        welcomeLabel.setText("Welcome, " + student.getName());
        loadInitialData();
        loadRecommendedBooks(); // NEW
        // Update chatListView for the correct student after switching
        if (chatListView != null) {
            chatListView.setItems(ChatService.getMessagesForRecipient(student.getEmail()));
            ChatService.getMessages().addListener((ListChangeListener<ChatMessage>) c -> {
                chatListView.setItems(ChatService.getMessagesForRecipient(student.getEmail()));
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            });
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
            showAlert("Error", "Failed to search books: " + e.getMessage());
        }
    }

    /**
     * Handles the action of requesting a book.
     * If the student has reached the maximum number of books they can borrow, an
     * error alert is shown.
     * If the request is successful, a success alert is shown and the requests table
     * is refreshed.
     * If an error occurs while submitting the request, an error alert is shown.
     * 
     * @param book The selected book.
     */
    @FXML
    private void handleBookRequest(Book book) {
        // Check if student has reached the maximum number of books they can borrow
        if (!currentStudent.canBorrowMore()) {
            showAlert("Error", "You have reached the maximum number of books you can borrow!");
            return;
        }

        try {
            // Set the due date to 2 weeks from now
            LocalDateTime dueDate = LocalDateTime.now().plusDays(14);

            // Submit the request
            boolean success = studentService.requestBook(currentStudent.getId(), book.getId(),
                    dueDate);

            if (success) {
                showAlert("Success", "Book request submitted successfully!");
                loadRequests(); // Refresh requests table
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

    @FXML
    private void handleMyAccount() {
        try {
            // Get current student
            if (currentStudent == null) {
                showAlert("Error", "Student information not found.");
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

            // Refresh student data after any changes
            Student updatedStudent = studentService.getStudentById(currentStudent.getId());
            if (updatedStudent != null) {
                setCurrentStudent(updatedStudent);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to open account dialog: " + e.getMessage());
        }
    }

    /**
     * Loads all books from the database and sets the items in the books table.
     * If an exception is thrown while loading the books, an error alert is shown.
     */
    private void loadBooks() {
        try {
            // Get all books
            List<Book> books = bookService.getAllBooks();

            // Set books table items
            booksTable.setItems(FXCollections.observableArrayList(books));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load books: " + e.getMessage());
        }
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

    private void showBookDetails(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/book_details_dialog.fxml"));
            Parent root = loader.load();
            BookDetailsDialogController controller = loader.getController();
            controller.setBook(book, false); // false for student view

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Book Details: " + book.getTitle());
            dialogStage.setScene(new Scene(root, 800, 600));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open book details: " + e.getMessage());
        }
    }

    @FXML
    private void handleRequestBook() {
        Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook == null) {
            showAlert("No Selection", "Please select a book to request.");
            return;
        }

        try {
            // Check if student can borrow more books
            if (currentStudent.getBooksBorrowed() >= currentStudent.getMaxBooks()) {
                showAlert("Cannot Request", "You have reached the maximum number of books you can borrow.");
                return;
            }

            // Submit the request
            studentService.requestBook(currentStudent.getId(), selectedBook.getId(), LocalDateTime.now().plusDays(14));
            showAlert("Success", "Book request submitted successfully!");
            loadRequests(); // Refresh requests table
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to submit book request: " + e.getMessage());
        }
    }

    /**
     * Sets up the books table by setting up its columns and adding double-click
     * handler to show book details.
     *
     * The columns are:
     * <ul>
     * <li>Title</li>
     * <li>Author</li>
     * <li>Status (Available/Not Available)</li>
     * </ul>
     *
     * When a book is double-clicked, its details are shown in a dialog.
     *
     * The request button is also enabled/disabled based on the selection in the
     * table. It is enabled only if a book is selected and it has at least one
     * available copy.
     */
    private void setupBooksTable() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        statusColumn.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            long availableCopies = book.getCopies().stream()
                    .filter(copy -> "Available".equals(copy.getStatus()))
                    .count();
            return new SimpleStringProperty(availableCopies > 0 ? "Available" : "Not Available");
        });
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("availableCopiesCount"));

        // Add double-click handler for book details
        booksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    showBookDetails(selectedBook);
                }
            }
        });

        // Enable/disable request button based on selection
        requestBookButton.setDisable(true);
        booksTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    boolean isEnabled = newSelection != null &&
                            newSelection.getCopies().stream()
                                    .anyMatch(copy -> "Available".equals(copy.getStatus()));
                    requestBookButton.setDisable(!isEnabled);
                });
    }

    /**
     * Sets up the requests table by setting up its columns.
     * <p>
     * The columns are:
     * <ul>
     * <li>Book Title</li>
     * <li>Request Date</li>
     * <li>Due Date</li>
     * <li>Status</li>
     * </ul>
     */
    private void setupRequestsTable() {
        // Book title column
        requestBookTitleColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getBook().getTitle()));

        // Request date column
        requestDateColumn.setCellValueFactory(new PropertyValueFactory<>("requestDate"));

        // Due date column
        requestDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        // Status column
        requestStatusColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
    }

    private void setupBorrowingsTable() {
        borrowingBookTitleColumn
                .setCellValueFactory(
                        cellData -> new SimpleStringProperty(cellData.getValue().getBookCopy().getBook().getTitle()));
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
    }

    private void setupRecommendedBooksTable() {
        if (recommendedBooksTable == null)
            return;
        recommendedTitleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        recommendedAuthorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        recommendedStatusColumn.setCellValueFactory(cellData -> {
            Book book = cellData.getValue();
            long availableCopies = book.getCopies().stream()
                    .filter(copy -> "Available".equals(copy.getStatus()))
                    .count();
            return new SimpleStringProperty(availableCopies > 0 ? "Available" : "Not Available");
        });
        // Double-click to show details
        recommendedBooksTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book selectedBook = recommendedBooksTable.getSelectionModel().getSelectedItem();
                if (selectedBook != null) {
                    showBookDetails(selectedBook);
                }
            }
        });
    }

    private void loadRecommendedBooks() {
        if (recommendedBooksTable == null || currentStudent == null)
            return;
        try {
            List<Book> allBooks = bookService.getAllBooks();
            List<Borrowing> borrowings = borrowingService.getStudentBorrowings(currentStudent.getId());
            List<Long> borrowedBookIds = borrowings.stream()
                    .map(b -> b.getBookCopy().getBook().getId())
                    .toList();
            List<BookRequest> requests = studentService.getStudentRequests(currentStudent.getId());
            List<Long> requestedBookIds = requests.stream()
                    .map(r -> r.getBook().getId())
                    .toList();
            // Exclude already borrowed/requested books
            List<Book> notYetBorrowed = allBooks.stream()
                    .filter(b -> !borrowedBookIds.contains(b.getId()) && !requestedBookIds.contains(b.getId()))
                    .toList();

            // 1. Favorite categories (by borrow count)
            java.util.Map<String, Long> categoryCount = borrowings.stream()
                    .map(b -> b.getBookCopy().getBook().getCategory())
                    .filter(cat -> cat != null)
                    .collect(
                            java.util.stream.Collectors.groupingBy(cat -> cat, java.util.stream.Collectors.counting()));
            List<String> favoriteCategories = categoryCount.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                    .map(java.util.Map.Entry::getKey)
                    .toList();
            List<Book> fromFavoriteCategories = notYetBorrowed.stream()
                    .filter(b -> favoriteCategories.contains(b.getCategory()))
                    .limit(3)
                    .toList();

            // 2. New arrivals (recently added)
            List<Book> newArrivals = notYetBorrowed.stream()
                    .sorted((b1, b2) -> b2.getCreatedAt().compareTo(b1.getCreatedAt()))
                    .filter(b -> !fromFavoriteCategories.contains(b))
                    .limit(2)
                    .toList();

            // 3. Popular books (by borrow count)
            List<Book> popularBooks = notYetBorrowed.stream()
                    .filter(b -> !fromFavoriteCategories.contains(b) && !newArrivals.contains(b))
                    .sorted((b1, b2) -> {
                        int b2Count = (int) borrowingService.getBorrowingsByBook(b2.getId()).size();
                        int b1Count = (int) borrowingService.getBorrowingsByBook(b1.getId()).size();
                        return Integer.compare(b2Count, b1Count);
                    })
                    .limit(2)
                    .toList();

            // Combine, remove duplicates, and limit to 5
            List<Book> recommendations = new java.util.ArrayList<>();
            for (Book b : fromFavoriteCategories)
                if (!recommendations.contains(b))
                    recommendations.add(b);
            for (Book b : newArrivals)
                if (!recommendations.contains(b))
                    recommendations.add(b);
            for (Book b : popularBooks)
                if (!recommendations.contains(b))
                    recommendations.add(b);
            if (recommendations.size() > 5)
                recommendations = recommendations.subList(0, 5);

            recommendedBooksTable.setItems(FXCollections.observableArrayList(recommendations));
        } catch (Exception e) {
            recommendedBooksTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadInitialData() {
        loadBooks();
        loadRequests();
        loadBorrowings();
        loadRecommendedBooks(); // NEW
    }

    private void loadBorrowings() {
        try {
            List<Borrowing> borrowings = borrowingService.getStudentBorrowings(currentStudent.getId());
            borrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load borrowings: " + e.getMessage());
        }
    }

    @FXML
    private void handleSendChatMessage() {
        String text = chatInputField.getText();
        if (text != null && !text.trim().isEmpty()) {
            String sender = (currentStudent != null) ? currentStudent.getName() : "Client";
            String recipientId = (currentStudent != null) ? currentStudent.getEmail() : null;
            ChatMessage message = new ChatMessage(sender, text.trim(), recipientId);
            ChatService.sendMessage(message);
            chatInputField.clear();
            // Refresh chatListView after sending
            if (chatListView != null) {
                chatListView.setItems(ChatService.getMessagesForRecipient(recipientId));
                chatListView.scrollTo(chatListView.getItems().size() - 1);
            }
        }
    }

    @FXML
    private void showChat() {
        // Hide all other views
        for (var node : chatView.getParent().getChildrenUnmodifiable()) {
            if (node != chatView)
                node.setVisible(false);
        }
        chatView.setVisible(true);
    }
}