package com.library.service;

import com.library.model.Book;
import com.library.model.BookCopy;
import com.library.model.Borrowing;
import com.library.model.Student;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BorrowingService {
    private static final double FINE_PER_DAY = 1.0; // $1 per day
    private final BookService bookService;

    public BorrowingService() {
        this.bookService = new BookService();
    }

    public Borrowing borrowBook(Student student, Book book, LocalDate dueDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            // Get an available copy of the book
            List<BookCopy> availableCopies = bookService.getAvailableCopies(book.getIsbn());
            if (availableCopies.isEmpty()) {
                throw new RuntimeException("No available copies of this book");
            }

            // Create new borrowing record
            Borrowing borrowing = new Borrowing();
            borrowing.setBookCopy(availableCopies.get(0));
            borrowing.setStudent(student);
            borrowing.setBorrowDate(LocalDate.now());
            borrowing.setDueDate(dueDate);

            // Update book copy status
            BookCopy copy = availableCopies.get(0);
            copy.setStatus("Borrowed");

            session.save(borrowing);
            session.update(copy);

            transaction.commit();
            return borrowing;
        }
    }

    public Borrowing returnBook(Long borrowingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Borrowing borrowing = session.get(Borrowing.class, borrowingId);
            if (borrowing == null) {
                throw new RuntimeException("Borrowing record not found");
            }

            // Update borrowing record
            LocalDate returnDate = LocalDate.now();
            borrowing.setReturnDate(returnDate);

            // Calculate fine if returned after due date
            if (borrowing.getDueDate().isBefore(returnDate)) {
                long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(), returnDate);
                double fine = daysOverdue * FINE_PER_DAY;
                borrowing.setFineAmount(fine);
            } else {
                borrowing.setFineAmount(0.0);
            }

            // Update book copy status
            BookCopy copy = borrowing.getBookCopy();
            copy.setStatus("Available");

            session.update(borrowing);
            session.update(copy);

            transaction.commit();
            return borrowing;
        }
    }

    public void updateFineAmount(Borrowing borrowing) {
        if (borrowing.getDueDate().isBefore(LocalDate.now())) {
            long daysOverdue = ChronoUnit.DAYS.between(borrowing.getDueDate(),
                    borrowing.getReturnDate() != null ? borrowing.getReturnDate() : LocalDate.now());
            double fine = daysOverdue * FINE_PER_DAY;
            borrowing.setFineAmount(fine);
        }
    }

    public void updateAllFines() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            // Get all unreturned and overdue borrowings
            String hql = "FROM Borrowing b WHERE b.returnDate IS NULL AND b.dueDate < :currentDate";
            List<Borrowing> overdueBorrowings = session.createQuery(hql, Borrowing.class)
                    .setParameter("currentDate", LocalDate.now())
                    .getResultList();

            // Update fines for each overdue borrowing
            for (Borrowing borrowing : overdueBorrowings) {
                updateFineAmount(borrowing);
                session.merge(borrowing);
            }

            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void payFine(Long borrowingId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();

            Borrowing borrowing = session.get(Borrowing.class, borrowingId);
            if (borrowing == null) {
                throw new RuntimeException("Borrowing record not found");
            }

            borrowing.setFinePaid(true);
            session.update(borrowing);

            transaction.commit();
        }
    }

    public List<Borrowing> getBorrowingsByStudent(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE b.student.id = :studentId",
                    Borrowing.class);
            query.setParameter("studentId", studentId);
            return query.list();
        }
    }

    public ObservableList<Borrowing> getOverdueBorrowings() {
        updateAllFines(); // Update fines before getting overdue borrowings
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE b.dueDate < :today AND b.returnDate IS NULL",
                    Borrowing.class);
            query.setParameter("today", LocalDate.now());
            List<Borrowing> borrowings = query.list();
            return FXCollections.observableArrayList(borrowings);
        }
    }

    public ObservableList<Borrowing> getBorrowingsWithFines() {
        updateAllFines(); // Update fines before getting borrowings with fines
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Borrowing b WHERE b.fineAmount > 0 AND b.finePaid = false";
            List<Borrowing> borrowings = session.createQuery(hql, Borrowing.class)
                    .getResultList();
            return FXCollections.observableArrayList(borrowings);
        }
    }

    public double getTotalUnpaidFinesByStudent(Long studentId) {
        updateAllFines(); // Update fines before calculating total
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Double> query = session.createQuery(
                    "select sum(fineAmount) from Borrowing where student.id = :studentId and finePaid = false",
                    Double.class);
            query.setParameter("studentId", studentId);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }

    public void addBorrowing(Borrowing borrowing) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(borrowing);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void updateBorrowing(Borrowing borrowing) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(borrowing);
            transaction.commit();
        }
    }

    public List<Borrowing> getAllBorrowings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Borrowing", Borrowing.class).list();
        }
    }

    public List<Borrowing> getActiveBorrowings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "from Borrowing where returnDate is null", Borrowing.class);
            return query.list();
        }
    }

    public List<Borrowing> getBorrowingsByBook(Long bookId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "from Borrowing b where b.bookCopy.book.id = :bookId", Borrowing.class);
            query.setParameter("bookId", bookId);
            return query.list();
        }
    }

    public List<Borrowing> getCurrentBorrowingsByStudent(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE b.student.id = :studentId AND b.returnDate IS NULL",
                    Borrowing.class);
            query.setParameter("studentId", studentId);
            return query.getResultList();
        }
    }

    public List<Borrowing> getBorrowingHistoryByStudent(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE b.student.id = :studentId AND b.returnDate IS NOT NULL ORDER BY b.returnDate DESC",
                    Borrowing.class);
            query.setParameter("studentId", studentId);
            return query.getResultList();
        }
    }

    public List<Borrowing> searchBorrowings(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE " +
                            "LOWER(b.bookCopy.book.title) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(b.student.name) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(b.student.email) LIKE LOWER(:searchTerm)",
                    Borrowing.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        }
    }

    public List<Borrowing> getFineBorrowings() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Borrowing> query = session.createQuery(
                    "FROM Borrowing b WHERE " +
                            "b.returnDate IS NULL AND " +
                            "b.dueDate < CURRENT_DATE",
                    Borrowing.class);
            return query.getResultList();
        }
    }
}