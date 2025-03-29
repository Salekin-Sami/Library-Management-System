package com.library.controller;

import com.library.model.Book;
import com.library.service.BookMetadataService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.concurrent.CompletableFuture;

public class BookSearchDialogController {
    @FXML
    private TextField searchField;
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
    private ProgressIndicator progressIndicator;

    private final BookMetadataService metadataService;
    private Book selectedBook;

    public BookSearchDialogController() {
        this.metadataService = new BookMetadataService();
        this.selectedBook = null;
    }

    @FXML
    public void initialize() {
        // Initialize table columns
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        authorColumn.setCellValueFactory(new PropertyValueFactory<>("author"));
        isbnColumn.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        publisherColumn.setCellValueFactory(new PropertyValueFactory<>("publisher"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("publicationYear"));

        // Add double-click handler for table
        resultsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Book book = resultsTable.getSelectionModel().getSelectedItem();
                if (book != null) {
                    selectedBook = book;
                    closeDialog();
                }
            }
        });

        // Add enter key handler for search field
        searchField.setOnAction(event -> handleSearch());
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            progressIndicator.setVisible(true);
            resultsTable.setItems(FXCollections.observableArrayList());

            CompletableFuture.runAsync(() -> {
                ObservableList<Book> results = metadataService.searchBooks(query);
                Platform.runLater(() -> {
                    resultsTable.setItems(results);
                    progressIndicator.setVisible(false);
                });
            });
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }

    public Book getSelectedBook() {
        return selectedBook;
    }
}