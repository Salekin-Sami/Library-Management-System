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

    public Student registerStudent(String email, String fullName,
            String studentId, String phoneNumber) {
        // First, register the user
        try (Connection conn = DatabaseUtil.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert into users table with student ID as password
                String userSql = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, 'student')";
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

                // Save student using Hibernate
                Session session = HibernateUtil.getSessionFactory().openSession();
                Transaction transaction = session.beginTransaction();
                try {
                    session.persist(student);
                    transaction.commit();
                    conn.commit();
                    return student;
                } catch (Exception e) {
                    if (transaction != null) {
                        transaction.rollback();
                    }
                    conn.rollback();
                    throw new RuntimeException("Failed to create student record: " + e.getMessage());
                } finally {
                    session.close();
                }
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Failed to register student: " + e.getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("Database error: " + e.getMessage());
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
        String sql = "INSERT INTO book_requests (student_id, book_id, status, due_date) VALUES (?, ?, 'pending', ?)";

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            stmt.setLong(2, bookId);
            stmt.setTimestamp(3, Timestamp.valueOf(dueDate));

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<BookRequest> getStudentRequests(Long studentId) {
        String sql = "SELECT * FROM book_requests WHERE student_id = ? ORDER BY request_date DESC";
        List<BookRequest> requests = new ArrayList<>();

        try (Connection conn = DatabaseUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, studentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                BookRequest request = new BookRequest();
                request.setId(rs.getLong("id"));
                request.setStudentId(rs.getLong("student_id"));
                request.setBookId(rs.getLong("book_id"));
                request.setRequestDate(rs.getTimestamp("request_date").toLocalDateTime());
                request.setStatus(rs.getString("status"));
                request.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
                requests.add(request);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public Student getStudentByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Student> query = session.createQuery(
                    "from Student where email = :email", Student.class);
            query.setParameter("email", email);
            return query.uniqueResult();
        }
    }
}