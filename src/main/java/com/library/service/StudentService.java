package com.library.service;

import com.library.model.Student;
import com.library.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.List;

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
}