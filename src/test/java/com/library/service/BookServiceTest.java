package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.util.TestHibernateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    private BookService bookService;
    private Book testBook;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        bookService = new BookService();
        session = TestHibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        try {
            // Clean up existing test data in correct order
            session.createQuery("delete from Borrowing").executeUpdate();
            session.createQuery("delete from BookCopy").executeUpdate();
            session.createQuery("delete from Book").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }

        // Start new transaction
        transaction = session.beginTransaction();

        // Create a test book with unique ISBN
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("TEST" + System.currentTimeMillis()); // Unique ISBN
        testBook.setCategory("Test Category");
        testBook.setPublisher("Test Publisher");
        testBook.setPublicationYear("2024");
        testBook.setDescription("Test Description");
    }

    @AfterEach
    void tearDown() {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    void testAddBook() {
        // Test adding a new book
        Book addedBook = bookService.addBook(testBook);
        transaction.commit();

        assertNotNull(addedBook.getId());
        assertEquals(testBook.getTitle(), addedBook.getTitle());
        assertEquals(testBook.getAuthor(), addedBook.getAuthor());
        assertEquals(testBook.getIsbn(), addedBook.getIsbn());

        // Verify that a copy was created
        assertFalse(addedBook.getCopies().isEmpty());
        BookCopy copy = addedBook.getCopies().iterator().next();
        assertEquals("Available", copy.getStatus());
    }

    @Test
    void testGetBookById() {
        // First add a book
        Book addedBook = bookService.addBook(testBook);
        transaction.commit();

        // Then retrieve it
        Book retrievedBook = bookService.getBookById(addedBook.getId());

        assertNotNull(retrievedBook);
        assertEquals(addedBook.getId(), retrievedBook.getId());
        assertEquals(addedBook.getTitle(), retrievedBook.getTitle());
        assertEquals(addedBook.getAuthor(), retrievedBook.getAuthor());
    }

    @Test
    void testSearchBooksByTitle() {
        // Add multiple books with unique ISBNs
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setIsbn("JAVA" + System.currentTimeMillis());
        book1.setCategory("Programming");
        book1.setPublisher("Publisher 1");
        book1.setPublicationYear("2024");
        bookService.addBook(book1);

        Book book2 = new Book();
        book2.setTitle("Python Programming");
        book2.setAuthor("Author 2");
        book2.setIsbn("PYTHON" + System.currentTimeMillis());
        book2.setCategory("Programming");
        book2.setPublisher("Publisher 2");
        book2.setPublicationYear("2024");
        bookService.addBook(book2);

        transaction.commit();

        // Search for books containing "Java"
        List<Book> searchResults = bookService.searchBooksByTitle("Java");

        assertFalse(searchResults.isEmpty());
        assertTrue(searchResults.stream().anyMatch(b -> b.getTitle().contains("Java")));
    }

    @Test
    void testUpdateBook() {
        // First add a book
        bookService.addBook(testBook);
        transaction.commit();

        // Start new transaction for update
        transaction = session.beginTransaction();

        // Update the book
        testBook.setTitle("Updated Title");
        bookService.updateBook(testBook);
        transaction.commit();

        // Start new transaction for retrieval
        transaction = session.beginTransaction();

        // Retrieve the updated book
        Book updatedBook = bookService.getBookById(testBook.getId());

        assertEquals("Updated Title", updatedBook.getTitle());
    }

    @Test
    void testGetAvailableBooks() {
        // Add a book
        Book addedBook = bookService.addBook(testBook);
        transaction.commit();

        // Get available books
        List<Book> availableBooks = bookService.getAvailableBooks();

        assertFalse(availableBooks.isEmpty());
        assertTrue(availableBooks.stream().anyMatch(b -> b.getId().equals(addedBook.getId())));
    }
}