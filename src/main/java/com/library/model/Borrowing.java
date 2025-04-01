/**
 * Represents a borrowing record in the library system.
 * This entity tracks the details of a book borrowing transaction,
 * including the student who borrowed the book, the book copy borrowed,
 * and the associated dates and fines.
 * 
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Entity - Marks this class as a JPA entity.</li>
 *   <li>@Table(name = "borrowings") - Maps this entity to the "borrowings" table in the database.</li>
 * </ul>
 * 
 * <p>Fields:</p>
 * <ul>
 *   <li>id - The unique identifier for the borrowing record.</li>
 *   <li>student - The student who borrowed the book (Many-to-One relationship).</li>
 *   <li>bookCopy - The specific copy of the book borrowed (Many-to-One relationship).</li>
 *   <li>borrowDate - The date when the book was borrowed.</li>
 *   <li>dueDate - The date by which the book should be returned.</li>
 *   <li>returnDate - The date when the book was actually returned.</li>
 *   <li>fineAmount - The fine amount calculated for overdue returns.</li>
 *   <li>finePaid - Indicates whether the fine has been paid.</li>
 *   <li>createdAt - The timestamp when the borrowing record was created.</li>
 *   <li>updatedAt - The timestamp when the borrowing record was last updated.</li>
 * </ul>
 * 
 * <p>Transient Methods:</p>
 * <ul>
 *   <li>isOverdue() - Checks if the borrowing is overdue based on the due date and return date.</li>
 *   <li>getDaysOverdue() - Calculates the number of days the borrowing is overdue.</li>
 *   <li>calculateFine() - Calculates the fine amount for overdue borrowing (10 taka per day).</li>
 * </ul>
 * 
 * <p>Constructor:</p>
 * <ul>
 *   <li>Borrowing() - Initializes default values for borrowDate, finePaid, createdAt, and updatedAt.</li>
 * </ul>
 * 
 * <p>Overrides:</p>
 * <ul>
 *   <li>toString() - Provides a string representation of the borrowing record.</li>
 * </ul>
 */
package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "borrowings")
public class Borrowing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "book_copy_id")
    private BookCopy bookCopy;

    @Column(name = "borrow_date")
    private LocalDate borrowDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount")
    private Double fineAmount;

    @Column(name = "fine_paid")
    private Boolean finePaid;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    public Borrowing() {
        this.borrowDate = LocalDate.now();
        this.finePaid = false;
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BookCopy getBookCopy() {
        return bookCopy;
    }

    public void setBookCopy(BookCopy bookCopy) {
        this.bookCopy = bookCopy;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(Double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public Boolean isFinePaid() {
        return finePaid;
    }

    public void setFinePaid(Boolean finePaid) {
        this.finePaid = finePaid;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Transient methods
    @Transient
    public boolean isOverdue() {
        return LocalDate.now().isAfter(dueDate) && returnDate == null;
    }

    @Transient
    public int getDaysOverdue() {
        if (!isOverdue()) {
            return 0;
        }
        return (int) (LocalDate.now().toEpochDay() - dueDate.toEpochDay());
    }

    @Transient
    public double calculateFine() {
        if (!isOverdue()) {
            return 0.0;
        }
        // Assuming fine is 10 taka per day
        return getDaysOverdue() * 10.0;
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", student=" + student.getName() +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", fineAmount=" + fineAmount +
                ", finePaid=" + finePaid +
                '}';
    }
}