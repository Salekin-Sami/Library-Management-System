package com.library.service;

import com.library.model.Student;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import com.library.model.StudentProfile;
import com.library.model.BookRequest;
import com.library.util.DatabaseUtil;
import org.mindrot.jbcrypt.BCrypt;
import com.library.model.UserRole;
import com.library.util.PasswordUtil;
import com.library.model.Book;
import com.library.model.RequestStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class StudentService {

    public void addStudent(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(student);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void updateStudent(Student student) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(student);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public void deleteStudent(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                // First check if student has any active borrowings
                String checkBorrowingsHql = "SELECT COUNT(*) FROM Borrowing b WHERE b.student.id = :studentId AND b.returnDate IS NULL";
                Long activeBorrowings = (Long) session.createQuery(checkBorrowingsHql)
                        .setParameter("studentId", studentId)
                        .getSingleResult();

                if (activeBorrowings > 0) {
                    throw new IllegalStateException(
                            "Cannot delete student with active borrowings. Please ensure all books are returned first.");
                }

                // Check if student has any borrowing history
                String checkHistoryHql = "SELECT COUNT(*) FROM Borrowing b WHERE b.student.id = :studentId";
                Long totalBorrowings = (Long) session.createQuery(checkHistoryHql)
                        .setParameter("studentId", studentId)
                        .getSingleResult();

                if (totalBorrowings > 0) {
                    // If there's borrowing history, show a confirmation dialog
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Delete Student");
                    alert.setHeaderText("Confirm Delete");
                    alert.setContentText(
                            "This student has borrowing history. Deleting will remove all their borrowing records. Do you want to continue?");

                    alert.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            // Delete all borrowings first
                            String deleteBorrowingsHql = "DELETE FROM Borrowing b WHERE b.student.id = :studentId";
                            session.createQuery(deleteBorrowingsHql)
                                    .setParameter("studentId", studentId)
                                    .executeUpdate();

                            // Then delete the student
                            Student student = session.get(Student.class, studentId);
                            if (student != null) {
                                session.remove(student);
                            }
                        }
                    });
                } else {
                    // No borrowing history, safe to delete
                    Student student = session.get(Student.class, studentId);
                    if (student != null) {
                        session.remove(student);
                    }
                }

                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public Student getStudentById(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Student.class, studentId);
        }
    }

    public List<Student> getAllStudents() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from Student", Student.class).list();
        }
    }

    public List<Student> searchStudentsByName(String name) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where lower(name) like lower(:name)", Student.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }

    public List<Student> searchStudentsByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where lower(email) like lower(:email)", Student.class);
            query.setParameter("email", "%" + email + "%");
            return query.list();
        }
    }

    public List<Student> searchStudentsByContactNumber(String contactNumber) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where contactNumber like :contactNumber", Student.class);
            query.setParameter("contactNumber", "%" + contactNumber + "%");
            return query.list();
        }
    }

    public Student getStudentByStudentId(String studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where studentId = :studentId", Student.class);
            query.setParameter("studentId", studentId);
            return query.uniqueResult();
        }
    }

    public List<Student> searchStudents(String searchTerm) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "FROM Student s WHERE " +
                            "LOWER(s.name) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(s.email) LIKE LOWER(:searchTerm) OR " +
                            "LOWER(s.studentId) LIKE LOWER(:searchTerm)",
                    Student.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            return query.getResultList();
        }
    }

    public Student registerStudent(String email, String fullName, String studentId, String phoneNumber) {
        Connection conn = null;
        Session session = null;
        Transaction hibernateTransaction = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();
            conn.setAutoCommit(false);

            // Insert into users table with student ID as password
            String userSql = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'STUDENT')";
            PreparedStatement userStmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, email);
            userStmt.setString(2, BCrypt.hashpw(studentId, BCrypt.gensalt())); // Use student ID as password
            userStmt.executeUpdate();

            // Create student record
            Student student = new Student();
            student.setEmail(email);
            student.setName(fullName);
            student.setStudentId(studentId);
            student.setContactNumber(phoneNumber);
            student.setRole(UserRole.STUDENT); // Set the role explicitly
            student.setBooksBorrowed(0); // Initialize books borrowed
            student.setMaxBooks(3); // Set default max books
            // Note: createdAt and updatedAt are set in the Student constructor

            // Save student using Hibernate
            session = HibernateUtil.getSessionFactory().openSession();
            hibernateTransaction = session.beginTransaction();
            session.persist(student);
            hibernateTransaction.commit();

            // Commit the database transaction
            conn.commit();

            return student;
        } catch (Exception e) {
            // Rollback both transactions if any error occurs
            try {
                if (hibernateTransaction != null && hibernateTransaction.isActive()) {
                    hibernateTransaction.rollback();
                }
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            throw new RuntimeException("Failed to register student: " + e.getMessage(), e);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException closeEx) {
                closeEx.printStackTrace();
            }
        }
    }

    public StudentProfile getStudentProfile(int userId) {
        String sql = "SELECT * FROM student_profiles WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new StudentProfile(
                        rs.getInt("user_id"),
                        rs.getString("full_name"),
                        rs.getString("student_id"),
                        rs.getString("phone_number"),
                        rs.getString("email"),
                        rs.getInt("books_borrowed"),
                        rs.getInt("max_books"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStudentProfile(Long userId, String phoneNumber, String email) {
        String sql = "UPDATE student_profiles SET phone_number = ?, email = ? WHERE user_id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, phoneNumber);
            stmt.setString(2, email);
            stmt.setLong(3, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean requestBook(Long studentId, Long bookId, LocalDateTime dueDate) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Student student = session.get(Student.class, studentId);
                Book book = session.get(Book.class, bookId);

                if (student == null || book == null) {
                    return false;
                }

                BookRequest request = new BookRequest();
                request.setStudent(student);
                request.setBook(book);
                request.setDueDate(dueDate);
                request.setStatus(RequestStatus.PENDING);

                session.persist(request);
                transaction.commit();
                return true;
            } catch (Exception e) {
                transaction.rollback();
                e.printStackTrace();
                return false;
            }
        }
    }

    public List<BookRequest> getStudentRequests(Long studentId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookRequest> query = session.createQuery(
                    "from BookRequest where student.id = :studentId order by requestDate desc",
                    BookRequest.class);
            query.setParameter("studentId", studentId);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public List<BookRequest> getAllPendingRequests() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BookRequest> query = session.createQuery(
                    "from BookRequest where status = :status order by requestDate desc",
                    BookRequest.class);
            query.setParameter("status", RequestStatus.PENDING);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void updateRequestStatus(Long requestId, RequestStatus status) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BookRequest request = session.get(BookRequest.class, requestId);
            if (request != null) {
                request.setStatus(status);
                session.merge(request);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    public Student getStudentByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where email = :email", Student.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }

    public void updateStudentPassword(Long studentId, String newPassword) throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                // Update password in users table
                String updateUserSql = "UPDATE users SET password_hash = ? WHERE id = ?";
                try (Connection conn = DatabaseUtil.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(updateUserSql)) {
                    stmt.setString(1, newPassword); // Store password as is (since it's the student ID)
                    stmt.setLong(2, studentId);
                    stmt.executeUpdate();
                }

                transaction.commit();
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        }
    }

    public boolean validatePassword(Long studentId, String currentPassword) throws SQLException {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();

            String sql = "SELECT password_hash FROM users WHERE id = (SELECT id FROM students WHERE id = ?)";
            Query query = session.createNativeQuery(sql);
            query.setParameter(1, studentId);
            String storedHash = (String) query.getSingleResult();

            session.getTransaction().commit();

            return PasswordUtil.verifyPassword(currentPassword, storedHash);
        }
    }
}