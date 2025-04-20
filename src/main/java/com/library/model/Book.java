package com.library.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;




/**
 * Represents a book entity in the library system.
 * This class is mapped to the "books" table in the database.
 * It contains details about a book, including its title, author, ISBN, publisher, 
 * edition, publication year, category, cover image URL, rating, and description.
 * It also tracks the creation and update timestamps and maintains a list of book copies.
 * 
 * <p>Annotations:
 * <ul>
 *   <li>@Entity - Marks this class as a JPA entity.</li>
 *   <li>@Table(name = "books") - Specifies the table name in the database.</li>
 *   <li>@Id - Marks the primary key field.</li>
 *   <li>@GeneratedValue(strategy = GenerationType.IDENTITY) - Specifies the primary key generation strategy.</li>
 *   <li>@Column - Maps fields to database columns with additional constraints.</li>
 *   <li>@OneToMany - Defines a one-to-many relationship with the BookCopy entity.</li>
 *   <li>@Transient - Marks methods that are not persisted in the database.</li>
 * </ul>
 * </p>
 * 
 * <p>Key Features:
 * <ul>
 *   <li>Tracks book details such as title, author, ISBN, and publisher.</li>
 *   <li>Maintains a list of book copies and provides methods to get total and available copies.</li>
 *   <li>Automatically updates the "updatedAt" field whenever a setter method is called.</li>
 *   <li>Provides utility methods to check availability and retrieve available copies.</li>
 * </ul>
 * </p>
 * 
 * <p>Fields:
 * <ul>
 *   <li>id - Unique identifier for the book.</li>
 *   <li>title - Title of the book (required).</li>
 *   <li>author - Author of the book (required).</li>
 *   <li>isbn - ISBN of the book (unique).</li>
 *   <li>publisher - Publisher of the book (required).</li>
 *   <li>edition - Edition of the book.</li>
 *   <li>publicationYear - Year the book was published.</li>
 *   <li>category - Category of the book (required).</li>
 *   <li>coverImageUrl - URL of the book's cover image.</li>
 *   <li>rating - Average rating of the book.</li>
 *   <li>description - Description of the book (stored as TEXT in the database).</li>
 *   <li>createdAt - Timestamp when the book was created.</li>
 *   <li>updatedAt - Timestamp when the book was last updated.</li>
 *   <li>copies - List of book copies associated with this book.</li>
 * </ul>
 * </p>
 * 
 * <p>Methods:
 * <ul>
 *   <li>Getters and setters for all fields.</li>
 *   <li>getTotalCopies() - Returns the total number of copies.</li>
 *   <li>getAvailableCopies() - Returns a list of available copies.</li>
 *   <li>isAvailable() - Checks if there are any available copies.</li>
 *   <li>toString() - Provides a string representation of the book.</li>
 * </ul>
 * </p>
 */
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true)
    private String isbn;

    @Column(nullable = false)
    private String publisher;

    private String edition;

    private String publicationYear;

    @Column(nullable = false)
    private String category;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    private Double rating;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BookCopy> copies = new ArrayList<>();

    // Default constructor
    public Book() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = LocalDate.now();
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
        this.updatedAt = LocalDate.now();
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
        this.updatedAt = LocalDate.now();
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
        this.updatedAt = LocalDate.now();
    }

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
        this.updatedAt = LocalDate.now();
    }

    public String getPublicationYear() {
        return publicationYear;
    }

    public void setPublicationYear(String publicationYear) {
        this.publicationYear = publicationYear;
        this.updatedAt = LocalDate.now();
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        this.updatedAt = LocalDate.now();
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
        this.updatedAt = LocalDate.now();
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
        this.updatedAt = LocalDate.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }

    @Transient
    public int getTotalCopies() {
        return copies.size();
    }

    @Transient
    public List<BookCopy> getAvailableCopies() {
        return copies.stream()
                .filter(BookCopy::isAvailable)
                .toList();
    }

    @Transient
    public boolean isAvailable() {
        return !getAvailableCopies().isEmpty();
    }

@Transient
public int getAvailableCopiesCount() {
    return (int) copies.stream()
                       .filter(BookCopy::isAvailable)
                       .count();
}

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publisher='" + publisher + '\'' +
                ", edition='" + edition + '\'' +
                ", publicationYear=" + publicationYear +
                ", category='" + category + '\'' +
                '}';
    }
}