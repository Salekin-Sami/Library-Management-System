package com.library.controller;

import com.library.model.Borrowing;
import com.library.model.Student;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentBorrowingHistoryController {
    @FXML
    private Label studentNameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private TableView<Borrowing> currentBorrowingsTable;
    @FXML
    private TableView<Borrowing> borrowingHistoryTable;
    @FXML
    private Button returnButton;

    // Current borrowings columns
    @FXML
    private TableColumn<Borrowing, String> bookTitleColumn;
    @FXML
    private TableColumn<Borrowing, Integer> copyNumberColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> borrowDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> dueDateColumn;
    @FXML
    private TableColumn<Borrowing, Double> fineColumn;
    @FXML
    private TableColumn<Borrowing, String> statusColumn;

    // History columns
    @FXML
    private TableColumn<Borrowing, String> historyBookTitleColumn;
    @FXML
    private TableColumn<Borrowing, Integer> historyCopyNumberColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> historyBorrowDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> historyDueDateColumn;
    @FXML
    private TableColumn<Borrowing, LocalDate> returnDateColumn;
    @FXML
    private TableColumn<Borrowing, Double> historyFineColumn;
    @FXML
    private TableColumn<Borrowing, String> historyStatusColumn;

    private Student student;
    private final BookService bookService;
    private final BorrowingService borrowingService;
    private final DateTimeFormatter dateFormatter;

    public StudentBorrowingHistoryController() {
        this.bookService = new BookService();
        this.borrowingService = new BorrowingService();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    }

    public void setStudent(Student student) {
        this.student = student;
        studentNameLabel.setText(student.getName());
        studentIdLabel.setText(student.getStudentId());
        loadBorrowings();
    }

    private void loadBorrowings() {
        // Load current borrowings
        List<Borrowing> currentBorrowings = borrowingService.getCurrentBorrowingsByStudent(student.getId());
        setupCurrentBorrowingsTable(currentBorrowings);

        // Load borrowing history
        List<Borrowing> history = borrowingService.getBorrowingHistoryByStudent(student.getId());
        setupBorrowingHistoryTable(history);

        // Enable/disable return button based on selection
        returnButton.setDisable(true);
        currentBorrowingsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> returnButton.setDisable(newSelection == null));
    }

    private void setupCurrentBorrowingsTable(List<Borrowing> borrowings) {
        bookTitleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getBookCopy().getBook().getTitle()));
        copyNumberColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getBookCopy().getCopyNumber()).asObject());
        borrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        dueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        fineColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        statusColumn.setCellValueFactory(data -> {
            Borrowing borrowing = data.getValue();
            String status = borrowing.isOverdue() ? "Overdue" : "Active";
            return new SimpleStringProperty(status);
        });

        // Custom cell factories for dates and fine
        borrowDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        dueDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                    if (date.isBefore(LocalDate.now())) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        fineColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double fine, boolean empty) {
                super.updateItem(fine, empty);
                if (empty || fine == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", fine));
                    if (fine > 0) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        currentBorrowingsTable.setItems(FXCollections.observableArrayList(borrowings));
    }

    private void setupBorrowingHistoryTable(List<Borrowing> history) {
        historyBookTitleColumn.setCellValueFactory(
                data -> new SimpleStringProperty(data.getValue().getBookCopy().getBook().getTitle()));
        historyCopyNumberColumn.setCellValueFactory(
                data -> new SimpleIntegerProperty(data.getValue().getBookCopy().getCopyNumber()).asObject());
        historyBorrowDateColumn.setCellValueFactory(new PropertyValueFactory<>("borrowDate"));
        historyDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        returnDateColumn.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        historyFineColumn.setCellValueFactory(new PropertyValueFactory<>("fineAmount"));
        historyStatusColumn.setCellValueFactory(data -> {
            Borrowing borrowing = data.getValue();
            String status = borrowing.isFinePaid() ? "Returned (Fine Paid)" : "Returned (Fine Unpaid)";
            return new SimpleStringProperty(status);
        });

        // Custom cell factories for dates
        historyBorrowDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        historyDueDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        returnDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });

        // Custom cell factory for fine
        historyFineColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double fine, boolean empty) {
                super.updateItem(fine, empty);
                if (empty || fine == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", fine));
                    if (fine > 0) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    } else {
                        setTextFill(javafx.scene.paint.Color.BLACK);
                    }
                }
            }
        });

        borrowingHistoryTable.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    private void handleReturnSelected() {
        Borrowing selectedBorrowing = currentBorrowingsTable.getSelectionModel().getSelectedItem();
        if (selectedBorrowing != null) {
            try {
                // Calculate fine if overdue
                if (selectedBorrowing.isOverdue()) {
                    double fine = selectedBorrowing.calculateFine();
                    selectedBorrowing.setFineAmount(fine);
                }

                // Set return date
                selectedBorrowing.setReturnDate(LocalDate.now());

                // Update book copy status
                bookService.updateCopyStatus(selectedBorrowing.getBookCopy(), "Available");

                // Update borrowing record
                borrowingService.updateBorrowing(selectedBorrowing);

                // Refresh tables
                loadBorrowings();

                // Show success message
                showAlert("Success", "Book returned successfully.", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Failed to return book: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}