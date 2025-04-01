# Library Management System - Technical Documentation

## Table of Contents
1. [Project Overview](#project-overview)
2. [System Architecture](#system-architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Core Components](#core-components)
6. [Database Design](#database-design)
7. [User Interface](#user-interface)
8. [Security Implementation](#security-implementation)
9. [Testing Strategy](#testing-strategy)
10. [Build and Deployment](#build-and-deployment)
11. [Development Workflow](#development-workflow)
12. [Performance Optimization](#performance-optimization)
13. [Security Measures](#security-measures)
14. [Future Roadmap](#future-roadmap)
## Project Overview

### Formal Introduction

The **Library Management System** is a comprehensive desktop application designed to streamline library operations. Built with modern Java technologies, it provides a robust, secure, and user-friendly interface for managing library resources, user accounts, and administrative functions. The system implements industry-standard practices for data management, security, and user experience.

### Introduction

Imagine managing a large library with thousands of books while keeping track of:

- Who borrowed which book
- When books need to be returned
- Where each book is located
- Who can access what
- How to keep everything organized

This system acts as an intelligent assistant that:

- Locates books instantly
- Tracks borrowing history
- Ensures proper access control
- Simplifies library management for librarians

---

## System Architecture

### Formal Architecture

The system follows a **layered architecture** pattern:

1. **Presentation Layer**: JavaFX-based UI components.
2. **Business Logic Layer**: Service classes handling core operations.
3. **Data Access Layer**: Hibernate ORM for database operations.
4. **Security Layer**: Authentication and authorization services.
5. **Integration Layer**: External service communication.

###

## Technology Stack

### Core Technologies

#### **Java 17**

- Universal programming language ensuring cross-platform compatibility.
- Key Features:
  - Pattern matching for switch expressions.
  - Improved garbage collection and performance optimizations.

#### **JavaFX 17.0.2**

- Modern UI framework with customizable UI components.
- Key Features:
  - Scene graph architecture.
  - CSS styling support.
  - Event-driven programming model.

#### **Maven**

- Dependency management and build automation tool.
- Key Features:
  - Project Object Model (POM) structure.
  - Plugin-based architecture.
  - Automated dependency resolution.

### Database Layer

#### **MySQL**

- High-performance relational database system.
- Key Features:
  - ACID compliance.
  - Transaction support.
  - Index optimization for fast queries.

#### **Hibernate 6.2.13**

- Object-Relational Mapping (ORM) framework for database interaction.
- Key Features:
  - Automatic SQL generation.
  - Caching mechanisms.
  - Lazy loading for performance efficiency.

### Security & Authentication

#### **BCrypt**

- Secure password hashing algorithm.
- Key Features:
  - Adaptive cost factor.
  - Protection against brute force attacks.

#### **JavaMail API**

- Facilitates email-based communication.
- Key Features:
  - SMTP, IMAP, and POP3 support.
  - MIME message handling.
  - Email attachment support.
  for future implementation

### Testing Framework

#### **JUnit 5**

- Unit testing framework for Java applications.
- Key Features:
  - Parameterized tests.
  - Test lifecycle hooks.
  - Dynamic test generation.

#### **Mockito**

- Mocking framework for Java unit tests.
- Key Features:
  - Mock creation and stubbing.
  - Verification of method calls.

### Additional Libraries

#### **OkHttp**

- HTTP client for network communication.
- Key Features:
  - Connection pooling.
  - WebSocket support.
  - Response caching.

#### **JSON Libraries**

- Enables serialization and deserialization of JSON data.
- Key Features:
  - Schema validation.
  - Streaming API for large JSON data.

## Project Structure

### Directory Structure
```
library-management-system/
├── src/
│   ├── main/                    # Main source code
│   │   ├── java/               # Java source files
│   │   │   ├── com.library/    # Main package
│   │   │   │   ├── controllers/  # UI controllers
│   │   │   │   ├── models/       # Data models
│   │   │   │   ├── services/     # Business logic
│   │   │   │   ├── utils/        # Utility classes
│   │   │   │   └── Main.java     # Application entry point
│   │   └── resources/          # Resource files
│   │       ├── fxml/           # UI layouts
│   │       ├── css/            # Stylesheets
│   │       └── config/         # Configuration files
│   └── test/                   # Test source code
├── target/                     # Compiled files
├── .github/                    # GitHub specific files
├── .vscode/                    # VS Code configuration
├── .settings/                  # Eclipse settings
├── pom.xml                     # Maven configuration
├── auth_schema.sql             # Authentication schema
├── classic_books.sql           # Sample book data
└── README.md                   # Project overview
```

### Detailed Package Structure

#### 1. Main Package (`com.library`)
- **controllers/**
  - `LoginController.java` - Handles user authentication
  - `DashboardController.java` - Manages main dashboard
  - `BookController.java` - Controls book operations
  - `UserController.java` - Manages user operations

- **models/**
  - `User.java` - User entity model
  - `Book.java` - Book entity model
  - `Transaction.java` - Transaction entity model
  - `StudentProfile.java` - Student profile model

- **services/**
  - `AuthService.java` - Authentication logic
  - `BookService.java` - Book management logic
  - `UserService.java` - User management logic
  - `TransactionService.java` - Transaction handling

- **utils/**
  - `DatabaseUtil.java` - Database connection management
  - `SecurityUtil.java` - Security-related utilities
  - `ValidationUtil.java` - Input validation
  - `LoggerUtil.java` - Logging utilities

#### 2. Resource Files
- **fxml/**
  - `login.fxml` - Login screen layout
  - `dashboard.fxml` - Main dashboard layout
  - `book_management.fxml` - Book management interface
  - `user_management.fxml` - User management interface

- **css/**
  - `styles.css` - Main stylesheet
  - `components.css` - Component-specific styles
  - `themes.css` - Theme variations

- **config/**
  - `database.properties` - Database configuration
  - `application.properties` - Application settings
  - `logging.properties` - Logging configuration

### SQL Files and Database Structure

#### 1. Authentication Schema (`auth_schema.sql`)
This file contains the core database structure for user authentication and management:

```sql
-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('admin', 'student') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    temp_password VARCHAR(255),
    temp_password_expiry TIMESTAMP,
    password_reset_required BOOLEAN DEFAULT FALSE
);
```
- Stores user authentication information
- Manages user roles and permissions
- Handles password reset functionality
- Tracks user creation timestamps

```sql
-- Student Profiles Table
CREATE TABLE IF NOT EXISTS student_profiles (
    user_id INT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    email VARCHAR(255) NOT NULL,
    books_borrowed INT DEFAULT 0,
    max_books INT DEFAULT 3,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```
- Stores detailed student information
- Tracks book borrowing limits
- Links to main user account
- Manages student contact information

```sql
-- Books Table
CREATE TABLE IF NOT EXISTS books (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(13) UNIQUE,
    category VARCHAR(100),
    publication_year INT,
    available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```
- Manages book inventory
- Tracks book availability
- Stores book metadata
- Maintains update timestamps

```sql
-- Book Requests Table
CREATE TABLE IF NOT EXISTS book_requests (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    book_id INT NOT NULL,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('pending', 'approved', 'rejected') DEFAULT 'pending',
    due_date TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
```
- Handles book borrowing requests
- Tracks request status
- Manages due dates
- Links students to books

#### 2. Sample Data (`classic_books.sql`)
This file contains initial book data for the system:

```sql
INSERT INTO books (title, author, isbn, category, publisher, publicationYear, status, created_at, updated_at) VALUES 
('Pride and Prejudice', 'Jane Austen', '9780141439518', 'Fiction', 'Penguin Classics', '1813', 'Available', NOW(), NOW()),
('To Kill a Mockingbird', 'Harper Lee', '9780446310789', 'Fiction', 'Grand Central Publishing', '1960', 'Available', NOW(), NOW()),
-- ... more books ...
```
- Provides initial book inventory
- Includes classic literature
- Contains complete book metadata
- Sets initial availability status

### Database Relationships

1. **User-Student Profile Relationship**
   - One-to-One relationship
   - Each user can have one student profile
   - Student profile is deleted when user is deleted (CASCADE)

2. **User-Book Request Relationship**
   - One-to-Many relationship
   - Each user can have multiple book requests
   - Book requests are linked to user ID

3. **Book-Book Request Relationship**
   - One-to-Many relationship
   - Each book can have multiple requests
   - Book requests are linked to book ID


### Database-Java Interaction

#### 1. Hibernate Entity Mapping
```java
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
    
    // Getters and setters
}
```
- Maps Java classes to database tables
- Handles data type conversion
- Manages relationships between entities
- Provides object-oriented database access

#### 2. Repository Pattern Implementation
```java
@Repository
public class BookRepository {
    @PersistenceContext
    private EntityManager entityManager;
    
    public Book findById(Long id) {
        return entityManager.find(Book.class, id);
    }
    
    public List<Book> findByTitle(String title) {
        return entityManager.createQuery(
            "SELECT b FROM Book b WHERE b.title LIKE :title", Book.class)
            .setParameter("title", "%" + title + "%")
            .getResultList();
    }
}
```
- Provides data access abstraction
- Encapsulates database operations
- Handles transaction management
- Implements CRUD operations

#### 3. Service Layer Integration
```java
@Service
@Transactional
public class BookService {
    @Autowired
    private BookRepository bookRepository;
    
    public Book addBook(Book book) {
        // Business logic validation
        validateBook(book);
        // Database operation
        return bookRepository.save(book);
    }
    
    public List<Book> searchBooks(String query) {
        // Business logic processing
        String searchQuery = processSearchQuery(query);
        // Database operation
        return bookRepository.findByTitle(searchQuery);
    }
}
```
- Implements business logic
- Manages transactions
- Handles data validation
- Coordinates multiple operations

#### 4. Transaction Management
```java
@Transactional
public class TransactionService {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserRepository userRepository;
    
    public void borrowBook(Long userId, Long bookId) {
        // Start transaction
        Book book = bookRepository.findById(bookId);
        User user = userRepository.findById(userId);
        
        // Business logic
        if (book.isAvailable() && user.canBorrow()) {
            book.setAvailable(false);
            bookRepository.save(book);
            // Create transaction record
            createTransaction(user, book);
        }
        // Commit transaction
    }
}
```
- Ensures data consistency
- Handles rollback scenarios
- Manages concurrent access
- Maintains data integrity

#### 5. Connection Management
```java
@Configuration
public class DatabaseConfig {
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url("jdbc:mysql://localhost:3306/library_db")
            .username("library_user")
            .password("secure_password")
            .build();
    }
    
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return Persistence.createEntityManagerFactory("library_persistence");
    }
}
```
- Manages database connections
- Implements connection pooling
- Handles connection lifecycle
- Provides connection configuration

#### 6. Error Handling
```java
@ControllerAdvice
public class DatabaseExceptionHandler {
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDatabaseError(DataAccessException ex) {
        // Log the error
        logger.error("Database error occurred", ex);
        
        // Create appropriate response
        ErrorResponse response = new ErrorResponse(
            "Database operation failed",
            ex.getMessage()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
```
- Handles database exceptions
- Provides meaningful error messages
- Implements recovery strategies
- Maintains application stability

#### 7. Data Validation
```java
@Component
public class BookValidator {
    public void validateBook(Book book) {
        List<String> errors = new ArrayList<>();
        
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            errors.add("Title cannot be empty");
        }
        
        if (book.getIsbn() != null && !isValidIsbn(book.getIsbn())) {
            errors.add("Invalid ISBN format");
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
```
- Validates data before database operations
- Ensures data integrity
- Prevents invalid data entry
- Provides clear error messages

#### 8. Caching Implementation
```java
@Cacheable(value = "books")
public class BookService {
    public Book getBook(Long id) {
        // Check cache first
        Book cachedBook = cacheManager.get("books", id);
        if (cachedBook != null) {
            return cachedBook;
        }
        
        // If not in cache, get from database
        Book book = bookRepository.findById(id);
        // Store in cache
        cacheManager.put("books", id, book);
        return book;
    }
}
```
- Implements caching strategies
- Improves performance
- Reduces database load
- Manages cache lifecycle

#### 9. Batch Processing
```java
@Service
public class BookImportService {
    @Transactional
    public void importBooks(List<Book> books) {
        // Process in batches
        int batchSize = 50;
        for (int i = 0; i < books.size(); i += batchSize) {
            List<Book> batch = books.subList(i, 
                Math.min(i + batchSize, books.size()));
            bookRepository.saveAll(batch);
            entityManager.flush();
        }
    }
}
```
- Handles large data sets
- Optimizes performance
- Manages memory usage
- Provides progress tracking

#### 10. Event Handling
```java
@EntityListeners(BookEventListener.class)
public class Book {
    // ... existing fields ...
}

@Component
public class BookEventListener {
    @PostPersist
    public void onBookAdded(Book book) {
        // Notify relevant services
        notificationService.notifyNewBook(book);
        searchService.indexBook(book);
    }
    
    @PostUpdate
    public void onBookUpdated(Book book) {
        // Update related systems
        searchService.updateIndex(book);
        cacheManager.invalidate("books", book.getId());
    }
}
```
- Handles database events
- Coordinates system updates
- Maintains system consistency
- Implements event-driven architecture

## Core Components

### 1. User Interface Components
- **Login Screen**
  - User authentication
  - Role-based access control
  - Password recovery

- **Dashboard**
  - Quick access to common functions
  - System status overview
  - Recent activities

- **Book Management**
  - Book catalog
  - Search functionality
  - Add/Edit/Delete books
  - Book status tracking

- **User Management**
  - User profiles
  - Role management
  - Access control

### 2. Business Logic Components
- **Authentication Service**
  - User validation
  - Session management
  - Password handling

- **Book Service**
  - Book operations
  - Inventory management
  - Availability checking

- **User Service**
  - User operations
  - Profile management
  - Permission handling

### 3. Data Access Components
- **Entity Classes**
  - Book entity
  - User entity
  - Transaction entity

- **Repository Classes**
  - Book repository
  - User repository
  - Transaction repository

## Database Design

### Library Database Overview

The library database (`library_db`) is the core data storage system that manages all library operations. It's designed to handle book inventory, user management, and transaction tracking efficiently.

### Database Schema

#### 1. Core Tables

##### Users Table
```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```
- **Purpose**: Stores user authentication and authorization information
- **Key Fields**:
  - `id`: Unique identifier for each user
  - `username`: User's login name (unique)
  - `password_hash`: Securely hashed password
  - `email`: User's email address (unique)
  - `role`: User's role (admin/student)
  - `created_at`: Account creation timestamp

##### Books Table
```sql
CREATE TABLE books (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    isbn VARCHAR(13) UNIQUE NOT NULL,
    publication_year INT,
    category VARCHAR(50),
    status VARCHAR(20) NOT NULL,
    location VARCHAR(50)
);
```
- **Purpose**: Manages book inventory and metadata
- **Key Fields**:
  - `id`: Unique identifier for each book
  - `title`: Book title
  - `author`: Book author
  - `isbn`: International Standard Book Number (unique)
  - `publication_year`: Year the book was published
  - `category`: Book category/genre
  - `status`: Current status (Available/Borrowed/Reserved)
  - `location`: Physical location in library

##### Transactions Table
```sql
CREATE TABLE transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    book_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
```
- **Purpose**: Tracks all book-related transactions
- **Key Fields**:
  - `id`: Unique identifier for each transaction
  - `user_id`: Reference to user who made the transaction
  - `book_id`: Reference to the book involved
  - `transaction_type`: Type of transaction (Borrow/Return/Reserve)
  - `transaction_date`: When the transaction occurred
  - `due_date`: When the book should be returned

### Database Operations

#### 1. Common Queries

##### Book Search
```sql
SELECT * FROM books 
WHERE title LIKE ? OR author LIKE ? OR isbn = ?;
```
- Searches books by title, author, or ISBN
- Uses LIKE for partial matches on text fields
- Exact match for ISBN

##### User Authentication
```sql
SELECT * FROM users 
WHERE username = ? AND password_hash = ?;
```
- Verifies user credentials
- Uses hashed password comparison
- Returns user details if match found

##### Active Borrowings
```sql
SELECT b.title, b.author, t.transaction_date, t.due_date
FROM transactions t
JOIN books b ON t.book_id = b.id
WHERE t.user_id = ? AND t.transaction_type = 'BORROW'
AND t.due_date > CURRENT_TIMESTAMP;
```
- Lists currently borrowed books for a user
- Shows due dates
- Only includes active borrowings

#### 2. Data Management

##### Adding New Books
```sql
INSERT INTO books (title, author, isbn, publication_year, category, status, location)
VALUES (?, ?, ?, ?, ?, 'Available', ?);
```
- Adds new books to inventory
- Sets initial status as 'Available'
- Requires all mandatory fields

##### Processing Book Returns
```sql
UPDATE books SET status = 'Available' WHERE id = ?;
INSERT INTO transactions (user_id, book_id, transaction_type, transaction_date)
VALUES (?, ?, 'RETURN', CURRENT_TIMESTAMP);
```
- Updates book status
- Records return transaction
- Maintains transaction history

### Database Optimization

#### 1. Indexes
```sql
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_isbn ON books(isbn);
CREATE INDEX idx_transactions_user ON transactions(user_id);
CREATE INDEX idx_transactions_book ON transactions(book_id);
```
- Improves search performance
- Optimizes foreign key lookups
- Speeds up common queries

#### 2. Constraints
```sql
ALTER TABLE transactions
ADD CONSTRAINT fk_user_transaction
FOREIGN KEY (user_id) REFERENCES users(id),
ADD CONSTRAINT fk_book_transaction
FOREIGN KEY (book_id) REFERENCES books(id);
```
- Ensures data integrity
- Prevents orphaned records
- Maintains referential integrity


## Future Roadmap

### Planned Features
1. **User Experience**
   - Mobile app
   - Web interface
   - Enhanced search

## Conclusion
This technical documentation provides a comprehensive overview of the Library Management System. The system is built with modern technologies and follows best practices in software development. Regular updates and maintenance will ensure the system remains secure, efficient, and user-friendly. 