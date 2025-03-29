package com.library.controller;

import com.library.model.Borrowing;
import com.library.service.BorrowingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;

public class BorrowingHistoryDialogController {
    @FXML
    private TableView<Borrowing> borrowingHistoryTable;

    @FXML
    private TableColumn<Borrowing, Long> idColumn;

    @FXML
    private TableColumn<Borrowing, Long> bookIdColumn;

    @FXML
    private TableColumn<Borrowing, Long> studentIdColumn;

    @FXML
    private TableColumn<Borrowing, LocalDate> borrowDateColumn;

    @FXML
    private TableColumn<Borrowing, LocalDate> dueDateColumn;

    @FXML
    private TableColumn<Borrowing, LocalDate> returnDateColumn;

    @FXML
    private TableColumn<Borrowing, Double> fineAmountColumn;

    @FXML
    private TableColumn<Borrowing, Boolean> finePaidColumn;

    private BorrowingService borrowingService;

    @FXML
    public void initialize() {
        borrowingService = new BorrowingService();
        setupTableColumns();
        loadBorrowingHistory();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookIdColumn.setCellValueFactory(new PropertyValueFactory<>("bookId"));
        studentIdColumn.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        fineAmountColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        finePaidColumn.setCellValueFactory(new PropertyValueFactory<>("finePaid"));
    }

    private void loadBorrowingHistory() {
        List<Borrowing> borrowings = borrowingService.getAllBorrowings();
        borrowingHistoryTable.setItems(FXCollections.observableArrayList(borrowings));
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) borrowingHistoryTable.getScene().getWindow();
        stage.close();
    }
}