package com.library.controller;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrowing;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BookDetailsDialogController {
    private final BookService bookService;
    private final BorrowingService borrowingService;
    private Book selectedBook;
    private boolean isLibrarianView;

    @FXML
    private Label titleLabel;
    @FXML
    private Label authorLabel;
    @FXML
    private Label isbnLabel;
    @FXML
    private Label publisherLabel;
    @FXML
    private Label publicationYearLabel;
    @FXML
    private Label categoryLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private Label totalCopiesLabel;
    @FXML
    private TableView<BookCopy> copiesTable;
    @FXML
    private TableColumn<BookCopy, Integer> copyNumberColumn;
    @FXML
    private TableColumn<BookCopy, String> copyStatusColumn;
    @FXML
    private TableColumn<BookCopy, String> copyLocationColumn;
    @FXML
    private TableColumn<BookCopy, Double> copyPriceColumn;
    @FXML
    private TableView<Borrowing> borrowingHistoryTable;
    @FXML
    private TableColumn<Borrowing, String> borrowerColumn;
    @FXML
    private TableColumn<Borrowing, String> borrowDateColumn;
    @FXML
    private TableColumn<Borrowing, String> dueDateColumn;
    @FXML
    private TableColumn<Borrowing, String> returnDateColumn;
    @FXML
    private TableColumn<Borrowing, String> fineColumn;
    @FXML
    private Button addCopyButton;
    @FXML
    private Button editButton;
    @FXML
    private ImageView coverImageView;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public BookDetailsDialogController() {
        this.bookService = new BookService();
        this.borrowingService = new BorrowingService();
    }

    public void setBook(Book book, boolean isLibrarianView) {
        this.selectedBook = book;
        this.isLibrarianView = isLibrarianView;

        // Set basic information with improved formatting
        titleLabel.setText(book.getTitle());
        authorLabel.setText("by " + book.getAuthor());
        isbnLabel.setText("ISBN: " + book.getIsbn());
        publisherLabel.setText(book.getPublisher());
        publicationYearLabel.setText(book.getPublicationYear());
        categoryLabel.setText(book.getCategory());
        statusLabel.setText(book.isAvailable() ? "Available" : "Not Available");
        statusLabel.getStyleClass().add(book.isAvailable() ? "status-available" : "status-unavailable");

        // Set description
        descriptionArea.setText(book.getDescription() != null && !book.getDescription().isEmpty()
                ? book.getDescription()
                : "No description available.");

        // Set total copies
        totalCopiesLabel.setText(String.valueOf(book.getTotalCopies()));

        // Load book cover if available
        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) {
            try {
                Image coverImage = new Image(book.getCoverImageUrl());
                coverImageView.setImage(coverImage);
            } catch (Exception e) {
                // If image loading fails, just clear the image
                coverImageView.setImage(null);
            }
        } else {
            // If no cover URL is available, just clear the image
            coverImageView.setImage(null);
        }

        // Setup tables
        setupCopiesTable();
        setupBorrowingHistoryTable();

        // Load data
        loadCopies();
        loadBorrowingHistory();

        // Show/hide librarian-specific controls
        addCopyButton.setVisible(isLibrarianView);
        editButton.setVisible(isLibrarianView);
    }

    private void setupCopiesTable() {
        copyNumberColumn.setCellValueFactory(new PropertyValueFactory<>("copyNumber"));
        copyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        copyLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        copyPriceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Add custom cell factory for status column
        copyStatusColumn.setCellFactory(column -> new TableCell<BookCopy, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    getStyleClass().clear();
                    getStyleClass().add(item.equals("Available") ? "status-available" : "status-unavailable");
                }
            }
        });
    }

    private void setupBorrowingHistoryTable() {
        borrowerColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getStudent().getName()));
        borrowDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatDate(cellData.getValue().getBorrowDate())));
        dueDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatDate(cellData.getValue().getDueDate())));
        returnDateColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatDate(cellData.getValue().getReturnDate())));
        fineColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(formatFine(cellData.getValue().calculateFine())));
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(dateFormatter) : "-";
    }

    private String formatFine(double fine) {
        return fine > 0 ? String.format("$%.2f", fine) : "-";
    }

    private void loadCopies() {
        copiesTable.setItems(FXCollections.observableArrayList(selectedBook.getCopies()));
    }

    private void loadBorrowingHistory() {
        List<Borrowing> history = borrowingService.getBorrowingsByBook(selectedBook.getId());
        borrowingHistoryTable.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    private void handleAddCopy() {
        try {
            bookService.addCopy(selectedBook);
            loadCopies();
            totalCopiesLabel.setText(String.valueOf(selectedBook.getTotalCopies()));
            showAlert("Success", "New copy added successfully!", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Failed to add copy: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        // TODO: Implement edit functionality
        showAlert("Info", "Edit functionality coming soon!", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
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