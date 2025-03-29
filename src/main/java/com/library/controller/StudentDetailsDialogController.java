package com.library.controller;

import com.library.model.Borrowing;
import com.library.model.Student;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentDetailsDialogController {
    @FXML
    private Label nameLabel;
    @FXML
    private Label studentIdLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label contactNumberLabel;
    @FXML
    private Label totalBorrowedLabel;
    @FXML
    private Label currentBorrowingsLabel;
    @FXML
    private Label unpaidFinesLabel;
    @FXML
    private Button editButton;
    @FXML
    private Button closeButton;

    @FXML
    private TableView<Borrowing> currentBorrowingsTable;
    @FXML
    private TableView<Borrowing> borrowingHistoryTable;

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
    private final BorrowingService borrowingService;
    private final DateTimeFormatter dateFormatter;

    public StudentDetailsDialogController() {
        this.borrowingService = new BorrowingService();
        this.dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    }

    public void setStudent(Student student) {
        this.student = student;
        updateStudentDetails();
        setupTables();
        loadBorrowings();
    }

    private void updateStudentDetails() {
        nameLabel.setText(student.getName());
        studentIdLabel.setText(student.getStudentId());
        emailLabel.setText(student.getEmail());
        contactNumberLabel.setText(student.getContactNumber());

        List<Borrowing> currentBorrowings = borrowingService.getCurrentBorrowingsByStudent(student.getId());
        List<Borrowing> allBorrowings = borrowingService.getBorrowingsByStudent(student.getId());
        double unpaidFines = borrowingService.getTotalUnpaidFinesByStudent(student.getId());

        totalBorrowedLabel.setText(String.valueOf(allBorrowings.size()));
        currentBorrowingsLabel.setText(String.valueOf(currentBorrowings.size()));
        unpaidFinesLabel.setText(String.format("$%.2f", unpaidFines));
    }

    private void setupTables() {
        // Setup current borrowings table
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

        // Setup history table
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

        setupDateFormatting();
    }

    private void setupDateFormatting() {
        // Add date formatting for all date columns
        borrowDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
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
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                    if (date.isBefore(LocalDate.now())) {
                        setTextFill(javafx.scene.paint.Color.RED);
                    }
                }
            }
        });

        historyBorrowDateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
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
                if (empty || date == null) {
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
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(date.format(dateFormatter));
                }
            }
        });
    }

    private void loadBorrowings() {
        List<Borrowing> currentBorrowings = borrowingService.getCurrentBorrowingsByStudent(student.getId());
        List<Borrowing> history = borrowingService.getBorrowingHistoryByStudent(student.getId());

        currentBorrowingsTable.setItems(FXCollections.observableArrayList(currentBorrowings));
        borrowingHistoryTable.setItems(FXCollections.observableArrayList(history));
    }

    @FXML
    private void handleEditStudent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add_student_dialog.fxml"));
            Scene scene = new Scene(loader.load());

            AddStudentDialogController controller = loader.getController();
            controller.setEditMode(student);

            Stage stage = new Stage();
            stage.setTitle("Edit Student");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

            // Refresh the student details
            updateStudentDetails();
        } catch (Exception e) {
            showAlert("Error", "Could not open edit student dialog: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClose() {
        ((Stage) closeButton.getScene().getWindow()).close();
    }

    private void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}