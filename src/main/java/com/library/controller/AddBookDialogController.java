package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import com.library.service.BookApiService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;
import java.util.Optional;

public class AddBookDialogController {
    @FXML
    private TextField searchField;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private TableView<Book> resultsTable;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> isbnColumn;
    @FXML
    private TableColumn<Book, String> publisherColumn;
    @FXML
    private TableColumn<Book, String> yearColumn;
    @FXML
    private Button addBookButton;

    private final BookService bookService;
    private final BookApiService bookApiService;
    private ObservableList<Book> searchResults;
    private Book selectedBook;

    public AddBookDialogController() {
        this.bookService = new BookService();
        this.bookApiService = new BookApiService();
        this.searchResults = FXCollections.observableArrayList();
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        resultsTable.setItems(searchResults);

        // Show add button only when a book is selected
        resultsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    selectedBook = newSelection;
                    addBookButton.setVisible(selectedBook != null);
                });
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            showAlert("Error", "Please enter a search query", Alert.AlertType.ERROR);
            return;
        }

        try {
            List<Book> books = bookApiService.searchBooks(query);
            searchResults.clear();
            searchResults.addAll(books);
            if (books.isEmpty()) {
                showAlert("No Results", "No books found for your search", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to search books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleAddSelectedBook() {
        if (selectedBook == null) {
            showAlert("No Book Selected", "Please select a book to add.", Alert.AlertType.WARNING);
            return;
        }

        try {
            if (bookService.bookExists(selectedBook.getIsbn())) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Book Already Exists");
                alert.setHeaderText("A book with this ISBN already exists");
                alert.setContentText("Would you like to add another copy of this book?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    Book existingBook = bookService.getBookByIsbn(selectedBook.getIsbn());
                    bookService.addCopy(existingBook);
                    showAlert("Success", "Added a new copy of the book.", Alert.AlertType.INFORMATION);
                }
            } else {
                bookService.addBook(selectedBook);
                showAlert("Success", "Book added successfully.", Alert.AlertType.INFORMATION);
            }
            closeDialog();
        } catch (Exception e) {
            showAlert("Error", "Failed to add book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}