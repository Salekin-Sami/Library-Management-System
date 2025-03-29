package com.library.service;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.*;
import java.util.stream.Collectors;

public class BookSuggestionService {
    private final BookService bookService;
    private final BorrowingService borrowingService;

    public BookSuggestionService() {
        this.bookService = new BookService();
        this.borrowingService = new BorrowingService();
    }

    public List<Book> getPersonalizedSuggestions(Long studentId, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Get student's borrowing history
            List<Borrowing> borrowings = borrowingService.getBorrowingsByStudent(studentId);

            if (borrowings.isEmpty()) {
                // If no borrowing history, return random available books
                return getRandomAvailableBooks(limit);
            }

            // Get categories from borrowing history
            Set<String> preferredCategories = borrowings.stream()
                    .map(borrowing -> borrowing.getBookCopy().getBook().getCategory())
                    .collect(Collectors.toSet());

            // Get books in preferred categories that have available copies
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b JOIN b.copies bc " +
                            "WHERE b.category IN :categories AND bc.status = 'Available' " +
                            "AND b.id NOT IN (SELECT br.bookCopy.book.id FROM Borrowing br WHERE br.student.id = :studentId)",
                    Book.class);
            query.setParameter("categories", preferredCategories);
            query.setParameter("studentId", studentId);
            query.setMaxResults(limit);

            List<Book> suggestions = query.list();

            // If not enough suggestions, add random available books
            if (suggestions.size() < limit) {
                suggestions.addAll(getRandomAvailableBooks(limit - suggestions.size()));
            }

            return suggestions;
        }
    }

    private List<Book> getRandomAvailableBooks(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b JOIN b.copies bc WHERE bc.status = 'Available' ORDER BY RAND()",
                    Book.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    public List<Book> getPopularBooks(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "select b from Book b join Borrowing br on b.id = br.book.id " +
                            "group by b.id order by count(br.id) desc",
                    Book.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    public List<Book> getBooksByCategory(String category, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "from Book where category = :category and status = 'Available'", Book.class);
            query.setParameter("category", category);
            query.setMaxResults(limit);
            return query.list();
        }
    }

    public List<Book> getHighlyRatedBooks(int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "from Book where rating >= 4.0 and status = 'Available' order by rating desc", Book.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
}