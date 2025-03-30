package com.library.controller;

import com.library.model.Student;
import com.library.model.Borrowing;
import com.library.service.BorrowingService;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDate;
import java.util.List;

public class StudentHistoryController {
    @FXML
    private TableView<Borrowing> historyTable;
    @FXML
    private TableColumn<Borrowing, String> bookColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<Borrowing, String> statusColumn;
    @FXML
    private TableColumn<Borrowing, String> fineColumn;

    private BorrowingService borrowingService = new BorrowingService();
    private Student student;

    @FXML
    public void initialize() {
        setupTable();
    }

    private void setupTable() {
        bookColumn.setCellValueFactory(
                cellData -> new SimpleStringProperty(cellData.getValue().getBookCopy().getBook().getTitle()));
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        statusColumn.setCellValueFactory(cellData -> {
            Borrowing borrowing = cellData.getValue();
            String status = borrowing.getReturnDate() != null ? "Returned" : "Borrowed";
            if (borrowing.getReturnDate() == null && borrowing.getDueDate().isBefore(LocalDate.now())) {
                status = "Overdue";
            }
            return new SimpleStringProperty(status);
        });
        fineColumn.setCellValueFactory(cellData -> {
            double fineAmount = cellData.getValue().calculateFine();
            return new SimpleStringProperty(fineAmount > 0
                    ? String.format("$%.2f%s", fineAmount, cellData.getValue().isFinePaid() ? " (Paid)" : "")
                    : "");
        });
    }

    public void setStudent(Student student) {
        this.student = student;
        loadHistory();
    }

    private void loadHistory() {
        if (student != null) {
            List<Borrowing> history = borrowingService.getBorrowingHistoryByStudent(student.getId());
            historyTable.getItems().setAll(history);
        }
    }

    @FXML
    private void handleClose() {
        historyTable.getScene().getWindow().hide();
    }
}