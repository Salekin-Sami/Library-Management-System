package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class BookService {

    public Book addBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Create initial copy for the book
            BookCopy initialCopy = new BookCopy();
            initialCopy.setBook(book);
            initialCopy.setCopyNumber(1);
            initialCopy.setStatus("Available");
            initialCopy.setLocation("Main Library"); // Default location
            initialCopy.setPrice(0.0); // Default price

            session.save(book);
            session.save(initialCopy);

            transaction.commit();
            return book;
        }
    }

    public BookCopy addCopy(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Get the next copy number
            Query<Integer> query = session.createQuery(
                    "SELECT MAX(bc.copyNumber) FROM BookCopy bc WHERE bc.book = :book",
                    Integer.class);
            query.setParameter("book", book);
            Integer maxCopyNumber = query.getSingleResult();
            int nextCopyNumber = (maxCopyNumber != null) ? maxCopyNumber + 1 : 1;

            // Create new copy
            BookCopy newCopy = new BookCopy();
            newCopy.setBook(book);
            newCopy.setCopyNumber(nextCopyNumber);
            newCopy.setStatus("Available");
            newCopy.setLocation("Main Library"); // Default location
            newCopy.setPrice(0.0); // Default price

            session.save(newCopy);
            transaction.commit();
            return newCopy;
        }
    }

    public List<BookCopy> getAvailableCopies(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookCopy> query = session.createQuery(
                    "FROM BookCopy bc WHERE bc.book.isbn = :isbn AND bc.status = 'Available'",
                    BookCopy.class);
            query.setParameter("isbn", isbn);
            return query.getResultList();
        }
    }

    public void updateCopyStatus(BookCopy copy, String status) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            copy.setStatus(status);
            session.update(copy);
            transaction.commit();
        }
    }

    public void deleteBook(Book book) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Delete all copies first
            Query<BookCopy> query = session.createQuery(
                    "FROM BookCopy bc WHERE bc.book = :book",
                    BookCopy.class);
            query.setParameter("book", book);
            List<BookCopy> copies = query.getResultList();
            for (BookCopy copy : copies) {
                session.delete(copy);
            }

            // Then delete the book
            session.delete(book);
            transaction.commit();
        }
    }

    public List<Book> searchBooks(String query) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> hqlQuery = session.createQuery(
                    "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies WHERE " +
                            "LOWER(b.title) LIKE LOWER(:query) OR " +
                            "LOWER(b.author) LIKE LOWER(:query) OR " +
                            "LOWER(b.category) LIKE LOWER(:query)",
                    Book.class);
            hqlQuery.setParameter("query", "%" + query + "%");
            return hqlQuery.getResultList();
        }
    }

    public List<Book> getAllBooks() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "FROM Book b LEFT JOIN FETCH b.copies",
                    Book.class);
            return query.getResultList();
        }
    }

    public List<Book> getAvailableBooks() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b JOIN b.copies bc WHERE bc.status = 'Available'",
                    Book.class);
            return query.getResultList();
        }
    }

    public void updateBook(Book book) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public Book getBookById(Long bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "FROM Book b LEFT JOIN FETCH b.copies WHERE b.id = :id",
                    Book.class);
            query.setParameter("id", bookId);
            return query.uniqueResult();
        }
    }

    public List<Book> searchBooksByTitle(String title) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies WHERE lower(b.title) like lower(:title)",
                    Book.class);
            query.setParameter("title", "%" + title + "%");
            return query.list();
        }
    }

    public List<Book> searchBooksByAuthor(String author) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies WHERE lower(b.author) like lower(:author)",
                    Book.class);
            query.setParameter("author", "%" + author + "%");
            return query.list();
        }
    }

    public List<Book> searchBooksByCategory(String category) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "SELECT DISTINCT b FROM Book b LEFT JOIN FETCH b.copies WHERE lower(b.category) like lower(:category)",
                    Book.class);
            query.setParameter("category", "%" + category + "%");
            return query.list();
        }
    }

    public void addClassicBooks() {
        List<Book> classicBooks = Arrays.asList(
                createBook("Pride and Prejudice", "Jane Austen", "9780141439518", "Fiction", "Penguin Classics",
                        "1813"),
                createBook("To Kill a Mockingbird", "Harper Lee", "9780446310789", "Fiction",
                        "Grand Central Publishing", "1960"),
                createBook("1984", "George Orwell", "9780451524935", "Fiction", "Signet Classic", "1949"),
                createBook("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", "Fiction", "Scribner", "1925"),
                createBook("One Hundred Years of Solitude", "Gabriel García Márquez", "9780060883287", "Fiction",
                        "Harper Perennial", "1967"),
                createBook("Don Quixote", "Miguel de Cervantes", "9780142437230", "Fiction", "Penguin Classics",
                        "1605"),
                createBook("The Odyssey", "Homer", "9780140268867", "Poetry", "Penguin Classics", "-800"),
                createBook("War and Peace", "Leo Tolstoy", "9780140447934", "Fiction", "Penguin Classics", "1869"),
                createBook("The Divine Comedy", "Dante Alighieri", "9780142437223", "Poetry", "Penguin Classics",
                        "1320"),
                createBook("Crime and Punishment", "Fyodor Dostoevsky", "9780143058142", "Fiction", "Penguin Classics",
                        "1866"),
                createBook("Jane Eyre", "Charlotte Brontë", "9780141441146", "Fiction", "Penguin Classics", "1847"),
                createBook("The Canterbury Tales", "Geoffrey Chaucer", "9780140424386", "Poetry", "Penguin Classics",
                        "1400"),
                createBook("Moby-Dick", "Herman Melville", "9780142437247", "Fiction", "Penguin Classics", "1851"),
                createBook("The Brothers Karamazov", "Fyodor Dostoevsky", "9780374528379", "Fiction",
                        "Farrar, Straus and Giroux", "1880"),
                createBook("Wuthering Heights", "Emily Brontë", "9780141439556", "Fiction", "Penguin Classics", "1847"),
                createBook("Les Misérables", "Victor Hugo", "9780140444308", "Fiction", "Penguin Classics", "1862"),
                createBook("The Picture of Dorian Gray", "Oscar Wilde", "9780141439570", "Fiction", "Penguin Classics",
                        "1890"),
                createBook("The Count of Monte Cristo", "Alexandre Dumas", "9780140449266", "Fiction",
                        "Penguin Classics", "1844"),
                createBook("Anna Karenina", "Leo Tolstoy", "9780143035008", "Fiction", "Penguin Classics", "1877"),
                createBook("Frankenstein", "Mary Shelley", "9780141439471", "Fiction", "Penguin Classics", "1818"));

        for (Book book : classicBooks) {
            try {
                // Check if book with same ISBN exists
                if (!bookExists(book.getIsbn())) {
                    addBook(book);
                }
            } catch (Exception e) {
                System.err.println("Failed to add book: " + book.getTitle() + " - " + e.getMessage());
            }
        }
    }

    public boolean bookExists(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(*) FROM Book b WHERE b.isbn = :isbn",
                    Long.class);
            query.setParameter("isbn", isbn);
            return query.getSingleResult() > 0;
        }
    }

    public Book getBookByIsbn(String isbn) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Book> query = session.createQuery(
                    "FROM Book b LEFT JOIN FETCH b.copies WHERE b.isbn = :isbn",
                    Book.class);
            query.setParameter("isbn", isbn);
            return query.uniqueResult();
        }
    }

    private Book createBook(String title, String author, String isbn, String category, String publisher, String year) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setPublisher(publisher);
        book.setPublicationYear(year);
        return book;
    }
}