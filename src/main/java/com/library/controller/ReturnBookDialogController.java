package com.library.controller;

import com.library.model.BookCopy;
import com.library.model.Borrowing;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReturnBookDialogController {
    @FXML
    private Label bookTitleLabel;
    @FXML
    private Label copyNumberLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private Label borrowDateLabel;
    @FXML
    private Label dueDateLabel;
    @FXML
    private Label fineAmountLabel;
    @FXML
    private CheckBox finePaidCheckBox;

    private Borrowing borrowing;
    private final BookService bookService;
    private final BorrowingService borrowingService;
    private final DateTimeFormatter dateFormatter;

    public ReturnBookDialogController() {
        this.bookService = new BookService();
        this.borrowingService = new BorrowingService();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    }

    public void setBorrowing(Borrowing borrowing) {
        this.borrowing = borrowing;
        BookCopy bookCopy = borrowing.getBookCopy();

        // Book details
        bookTitleLabel.setText(bookCopy.getBook().getTitle());
        copyNumberLabel.setText(String.format("Copy #%d", bookCopy.getCopyNumber()));
        locationLabel.setText(bookCopy.getLocation());

        // Student details
        studentNameLabel.setText(borrowing.getStudent().getName());
        studentIdLabel.setText(borrowing.getStudent().getStudentId());

        // Borrowing details
        borrowDateLabel.setText(borrowing.getBorrowDate().format(dateFormatter));
        dueDateLabel.setText(borrowing.getDueDate().format(dateFormatter));

        // Fine details
        double fine = borrowing.calculateFine();
        fineAmountLabel.setText(String.format("$%.2f", fine));
        borrowing.setFineAmount(fine);
        finePaidCheckBox.setSelected(borrowing.isFinePaid());

        // Style overdue items
        if (borrowing.isOverdue()) {
            fineAmountLabel.setStyle("-fx-text-fill: red;");
            dueDateLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleReturnBook() {
        try {
            // Update book copy status
            BookCopy bookCopy = borrowing.getBookCopy();
            bookService.updateCopyStatus(bookCopy, "Available");

            // Update borrowing record
            borrowing.setReturnDate(LocalDate.now());
            borrowing.setFinePaid(finePaidCheckBox.isSelected());
            borrowingService.updateBorrowing(borrowing);

            // Close dialog
            closeDialog();
        } catch (Exception e) {
            showAlert("Error", "Failed to return book: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) bookTitleLabel.getScene().getWindow();
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