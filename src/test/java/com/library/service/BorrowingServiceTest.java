package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrowing;
import com.library.model.Student;
import com.library.util.TestHibernateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    private BorrowingService borrowingService;
    private BookService bookService;
    private StudentService studentService;
    private Book testBook;
    private Student testStudent;
    private BookCopy testCopy;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        borrowingService = new BorrowingService();
        bookService = new BookService();
        studentService = new StudentService();
        session = TestHibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        try {
            // Clean up existing test data in correct order
            session.createQuery("delete from Borrowing").executeUpdate();
            session.createQuery("delete from BookCopy").executeUpdate();
            session.createQuery("delete from Book").executeUpdate();
            session.createQuery("delete from Student").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }

        // Start new transaction
        transaction = session.beginTransaction();

        try {
            // Create test book
            testBook = new Book();
            testBook.setTitle("Test Book");
            testBook.setAuthor("Test Author");
            testBook.setIsbn("TEST" + System.currentTimeMillis());
            testBook.setCategory("Test Category");
            testBook.setPublisher("Test Publisher");
            testBook.setPublicationYear("2024");
            testBook.setDescription("Test Description");
            testBook = bookService.addBook(testBook);
            testCopy = testBook.getCopies().iterator().next();

            // Create test student
            testStudent = new Student();
            testStudent.setName("Test Student");
            testStudent.setStudentId("STU" + System.currentTimeMillis());
            testStudent.setEmail("test@example.com");
            testStudent.setContactNumber("1234567890");
            studentService.addStudent(testStudent);

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }
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
    void testBorrowBook() {
        // Test borrowing a book
        LocalDate dueDate = LocalDate.now().plusDays(14);
        Borrowing borrowing = borrowingService.borrowBook(testStudent, testBook, dueDate);

        assertNotNull(borrowing);
        assertNotNull(borrowing.getId());
        assertEquals(testStudent.getId(), borrowing.getStudent().getId());
        assertEquals(testCopy.getId(), borrowing.getBookCopy().getId());
        assertEquals(LocalDate.now(), borrowing.getBorrowDate());
        assertEquals(dueDate, borrowing.getDueDate());
        assertEquals("Borrowed", borrowing.getBookCopy().getStatus());
    }

    @Test
    void testReturnBook() {
        // First borrow the book
        LocalDate dueDate = LocalDate.now().plusDays(14);
        Borrowing borrowing = borrowingService.borrowBook(testStudent, testBook, dueDate);

        // Then return it
        Borrowing returnedBorrowing = borrowingService.returnBook(borrowing.getId());

        // Verify the return
        assertNotNull(returnedBorrowing.getReturnDate());
        assertEquals("Available", returnedBorrowing.getBookCopy().getStatus());
    }

    @Test
    void testCalculateFine() {
        // Borrow the book with a past due date
        LocalDate pastDueDate = LocalDate.now().minusDays(5);
        Borrowing borrowing = borrowingService.borrowBook(testStudent, testBook, pastDueDate);

        // Return the book
        Borrowing returnedBorrowing = borrowingService.returnBook(borrowing.getId());

        // Verify fine calculation
        assertNotNull(returnedBorrowing.getFineAmount());
        assertTrue(returnedBorrowing.getFineAmount() > 0);
    }

    @Test
    void testGetActiveBorrowings() {
        // Borrow the book
        LocalDate dueDate = LocalDate.now().plusDays(14);
        borrowingService.borrowBook(testStudent, testBook, dueDate);

        // Get active borrowings
        List<Borrowing> activeBorrowings = borrowingService.getActiveBorrowings();

        assertFalse(activeBorrowings.isEmpty());
        assertTrue(activeBorrowings.stream().anyMatch(b -> b.getReturnDate() == null));
    }

    @Test
    void testGetBorrowingsByBook() {
        // Borrow the book
        LocalDate dueDate = LocalDate.now().plusDays(14);
        borrowingService.borrowBook(testStudent, testBook, dueDate);

        // Get borrowings for the book
        List<Borrowing> bookBorrowings = borrowingService.getBorrowingsByBook(testBook.getId());

        assertFalse(bookBorrowings.isEmpty());
        assertTrue(bookBorrowings.stream().allMatch(b -> b.getBookCopy().getBook().getId().equals(testBook.getId())));
    }
}