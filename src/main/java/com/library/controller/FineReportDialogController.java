package com.library.controller;

import com.library.model.Borrowing;
import com.library.service.BorrowingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class FineReportDialogController {
    @FXML
    private TableView<Borrowing> fineTable;
    @FXML
    private TableColumn<Borrowing, String> bookTitleColumn;
    @FXML
    private TableColumn<Borrowing, String> studentNameColumn;
    @FXML
    private TableColumn<Borrowing, String> dueDateColumn;
    @FXML
    private TableColumn<Borrowing, String> fineAmountColumn;
    @FXML
    private TableColumn<Borrowing, String> finePaidColumn;

    private Stage dialogStage;
    private BorrowingService borrowingService;
    private ObservableList<Borrowing> borrowings;

    public FineReportDialogController() {
        this.borrowings = FXCollections.observableArrayList();
    }

    @FXML
    private void initialize() {
        this.borrowingService = new BorrowingService();
        setupTableColumns();
        loadOverdueBorrowings();
    }

    private void setupTableColumns() {
        bookTitleColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getBookCopy().getBook().getTitle()));

        studentNameColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStudent().getName()));

        dueDateColumn
                .setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDueDate().toString()));

        fineAmountColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(String.format("%.2f", cellData.getValue().calculateFine())));

        finePaidColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().isFinePaid() ? "Yes" : "No"));
    }

    private void loadOverdueBorrowings() {
        borrowings.setAll(borrowingService.getOverdueBorrowings());
        fineTable.setItems(borrowings);
    }

    @FXML
    private void handlePayFine() {
        Borrowing selectedBorrowing = fineTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a borrowing to pay fine for.");
            return;
        }

        if (selectedBorrowing.isFinePaid()) {
            showAlert(Alert.AlertType.INFORMATION, "Already Paid",
                    "The fine for this borrowing has already been paid.");
            return;
        }

        try {
            borrowingService.payFine(selectedBorrowing.getId());
            loadOverdueBorrowings();
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Fine has been paid successfully.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred while paying the fine: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    @FXML
    private void handleClose() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }
}