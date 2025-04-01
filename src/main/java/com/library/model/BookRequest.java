/**
 * Represents a request made by a student to borrow a book from the library.
 * This entity is mapped to the "book_requests" table in the database.
 * It contains information about the student, the book, request and due dates, 
 * and the status of the request.
 * 
 * <p>Fields:</p>
 * <ul>
 *   <li><b>id</b>: The unique identifier for the book request.</li>
 *   <li><b>student</b>: The student who made the request. This is a many-to-one relationship.</li>
 *   <li><b>book</b>: The book being requested. This is a many-to-one relationship.</li>
 *   <li><b>requestDate</b>: The date and time when the request was made. Defaults to the current time.</li>
 *   <li><b>dueDate</b>: The date and time by which the book should be returned.</li>
 *   <li><b>status</b>: The current status of the request (e.g., PENDING, APPROVED, REJECTED).</li>
 * </ul>
 * 
 * <p>Annotations:</p>
 * <ul>
 *   <li><b>@Entity</b>: Marks this class as a JPA entity.</li>
 *   <li><b>@Table(name = "book_requests")</b>: Specifies the table name in the database.</li>
 *   <li><b>@Id</b>: Marks the primary key field.</li>
 *   <li><b>@GeneratedValue(strategy = GenerationType.IDENTITY)</b>: Specifies the primary key generation strategy.</li>
 *   <li><b>@ManyToOne</b>: Defines many-to-one relationships with Student and Book entities.</li>
 *   <li><b>@JoinColumn</b>: Specifies the foreign key columns for student and book relationships.</li>
 *   <li><b>@Column</b>: Maps fields to database columns with additional constraints.</li>
 *   <li><b>@Enumerated(EnumType.STRING)</b>: Maps the enum field to a string column in the database.</li>
 * </ul>
 * 
 * <p>Default Constructor:</p>
 * <ul>
 *   <li>Initializes the requestDate field to the current date and time.</li>
 * </ul>
 */
package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;



@Entity
@Table(name = "book_requests")
public class BookRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "due_date", nullable = false)
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    public BookRequest() {
        this.requestDate = LocalDateTime.now();
    }

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

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}