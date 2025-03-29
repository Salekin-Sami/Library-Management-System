package com.library.controller;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrowing;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookDetailsDialogController {
    private Book book;
    private boolean isLibrarianView;
    private final BookService bookService;
    private final BorrowingService borrowingService;

    @FXML
    private ImageView coverImageView;
    @FXML
    private Label titleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label publicationYearLabel;
    @FXML
    private Label editionLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label totalCopiesLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TableView<BookCopy> copiesTable;
    @FXML
    private TableColumn<BookCopy, String> copyNumberColumn;
    @FXML
    private TableColumn<BookCopy, String> locationColumn;
    @FXML
    private TableColumn<BookCopy, String> priceColumn;
    @FXML
    private TableColumn<BookCopy, String> statusColumn;
    @FXML
    private TableColumn<BookCopy, String> createdAtColumn;
    @FXML
    private TableColumn<BookCopy, String> updatedAtColumn;
    @FXML
    private TableView<Borrowing> borrowingHistoryTable;
    @FXML
    private TableColumn<Borrowing, String> borrowingStudentColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingDateColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingDueDateColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingReturnDateColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowingFineColumn;
    @FXML
    private Button editButton;
    @FXML
    private Button addCopyButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button closeButton;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public BookDetailsDialogController() {
        this.bookService = new BookService();
        this.borrowingService = new BorrowingService();
    }

    @FXML
    public void initialize() {
        // Load CSS
        String cssPath = getClass().getResource("/css/book_details.css").toExternalForm();
        if (cssPath != null) {
            copiesTable.getStylesheets().add(cssPath);
            borrowingHistoryTable.getStylesheets().add(cssPath);
        }

        // Setup table columns
        setupCopiesTable();
        setupBorrowingHistoryTable();

        // Set button visibility based on librarian view
        editButton.setVisible(false);
        addCopyButton.setVisible(false);
        deleteButton.setVisible(false);
    }

    private void setupCopiesTable() {
        copyNumberColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty("Copy #" + cellData.getValue().getCopyNumber()));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        priceColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("$%.2f", cellData.getValue().getPrice())));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        createdAtColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getCreatedAt().format(dateFormatter)));
        updatedAtColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getUpdatedAt().format(dateFormatter)));
    }

    private void setupBorrowingHistoryTable() {
        borrowingStudentColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudent().getName()));
        borrowingDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getBorrowDate().format(dateFormatter)));
        borrowingDueDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getDueDate().format(dateFormatter)));
        borrowingReturnDateColumn.setCellValueFactory(cellData -> {
            LocalDate returnDate = cellData.getValue().getReturnDate();
            return new SimpleStringProperty(returnDate != null ? returnDate.format(dateFormatter) : "");
        });
        borrowingFineColumn.setCellValueFactory(cellData -> {
            double fine = cellData.getValue().calculateFine();
            return new SimpleStringProperty(fine > 0 ? String.format("$%.2f", fine) : "");
        });
    }

    public void setBook(Book book, boolean isLibrarianView) {
        this.book = book;
        this.isLibrarianView = isLibrarianView;

        // Update UI elements
        titleLabel.setText(book.getTitle());
        authorLabel.setText(book.getAuthor());
        isbnLabel.setText("ISBN: " + book.getIsbn());
        categoryLabel.setText("Category: " + book.getCategory());
        publisherLabel.setText("Publisher: " + book.getPublisher());
        publicationYearLabel.setText("Publication Year: " + book.getPublicationYear());
        editionLabel.setText("Edition: " + book.getEdition());
        ratingLabel.setText("Rating: " + String.format("%.1f", book.getRating()));
        totalCopiesLabel.setText("Total Copies: " + book.getCopies().size());
        descriptionArea.setText(book.getDescription());

        // Load book cover
        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
            try {
                Image image = new Image(book.getCoverImageUrl());
                coverImageView.setImage(image);
            } catch (Exception e) {
                // Load default book cover image
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_book_cover.png"));
                coverImageView.setImage(defaultImage);
            }
        }

        // Load copies
        ObservableList<BookCopy> copies = FXCollections.observableArrayList(book.getCopies());
        copiesTable.setItems(copies);

        // Load borrowing history
        List<Borrowing> borrowings = borrowingService.getBorrowingsByBook(book.getId());
        ObservableList<Borrowing> borrowingHistory = FXCollections.observableArrayList(borrowings);
        borrowingHistoryTable.setItems(borrowingHistory);

        // Show/hide librarian controls
        editButton.setVisible(isLibrarianView);
        addCopyButton.setVisible(isLibrarianView);
        deleteButton.setVisible(isLibrarianView);
    }

    @FXML
    private void handleEdit() {
        // TODO: Implement edit functionality
    }

    @FXML
    private void handleAddCopy() {
        try {
            bookService.addCopy(book);
            // Refresh the copies table
            ObservableList<BookCopy> copies = FXCollections.observableArrayList(book.getCopies());
            copiesTable.setItems(copies);
            totalCopiesLabel.setText("Total Copies: " + book.getCopies().size());
        } catch (Exception e) {
            showAlert("Error", "Failed to add copy: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Book");
        alert.setHeaderText("Confirm Delete");
        alert.setContentText("Are you sure you want to delete this book? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    bookService.deleteBook(book);
                    closeDialog();
                } catch (Exception e) {
                    showAlert("Error", "Failed to delete book: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClose() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}