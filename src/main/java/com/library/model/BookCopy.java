/**
 * Represents a copy of a book in the library system.
 * Each book copy is associated with a specific book and contains details
 * such as its unique copy number, status, location, price, and timestamps
 * for creation and updates.
 * 
 * <p>This entity is mapped to the "book_copies" table in the database.</p>
 * 
 * <p>Key Features:</p>
 * <ul>
 *   <li>Each book copy has a unique identifier (id).</li>
 *   <li>Associated with a specific book through a many-to-one relationship.</li>
 *   <li>Tracks the status of the copy (e.g., "Available").</li>
 *   <li>Includes optional details such as location and price.</li>
 *   <li>Automatically updates the "updatedAt" field whenever a property changes.</li>
 *   <li>Provides a utility method to check if the copy is available.</li>
 * </ul>
 * 
 * <p>Default Values:</p>
 * <ul>
 *   <li>Status is set to "Available" by default.</li>
 *   <li>Creation and update timestamps are initialized to the current date.</li>
 * </ul>
 * 
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Entity: Marks this class as a JPA entity.</li>
 *   <li>@Table: Specifies the database table name ("book_copies").</li>
 *   <li>@Id and @GeneratedValue: Defines the primary key and its generation strategy.</li>
 *   <li>@ManyToOne and @JoinColumn: Maps the relationship with the Book entity.</li>
 *   <li>@Column: Maps fields to database columns with optional constraints.</li>
 *   <li>@Transient: Excludes the "isAvailable" method from persistence.</li>
 * </ul>
 */
package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;



@Entity
@Table(name = "book_copies")
public class BookCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "copy_number", nullable = false)
    private Integer copyNumber;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "location")
    private String location;

    @Column(name = "price")
    private Double price;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // Default constructor
    public BookCopy() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        this.status = "Available";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
        this.updatedAt = LocalDate.now();
    }

    public Integer getCopyNumber() {
        return copyNumber;
    }

    public void setCopyNumber(Integer copyNumber) {
        this.copyNumber = copyNumber;
        this.updatedAt = LocalDate.now();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = LocalDate.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDate.now();
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
        this.updatedAt = LocalDate.now();
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

    @Transient
    public boolean isAvailable() {
        return status != null && status.equals("Available");
    }
}