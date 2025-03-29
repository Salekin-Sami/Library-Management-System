package com.library.service;

import com.library.model.Student;
import com.library.model.UserRole;
import com.library.util.TestHibernateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    private StudentService studentService;
    private Student testStudent;
    private Session session;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        studentService = new StudentService();
        session = TestHibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();

        try {
            // Clean up existing test data in correct order
            session.createQuery("delete from Borrowing").executeUpdate();
            session.createQuery("delete from Student").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            throw e;
        }

        // Start new transaction
        transaction = session.beginTransaction();

        // Create test student with unique ID
        testStudent = new Student();
        testStudent.setName("Test Student");
        testStudent.setStudentId("STU" + System.currentTimeMillis());
        testStudent.setEmail("test@example.com");
        testStudent.setContactNumber("1234567890");
        testStudent.setRole(UserRole.STUDENT);
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
    void testAddStudent() {
        // Test adding a new student
        studentService.addStudent(testStudent);
        transaction.commit();

        // Verify student was added by retrieving it
        Student retrievedStudent = studentService.getStudentByStudentId(testStudent.getStudentId());
        assertNotNull(retrievedStudent);
        assertEquals(testStudent.getName(), retrievedStudent.getName());
        assertEquals(testStudent.getStudentId(), retrievedStudent.getStudentId());
        assertEquals(testStudent.getEmail(), retrievedStudent.getEmail());
        assertEquals(testStudent.getContactNumber(), retrievedStudent.getContactNumber());
    }

    @Test
    void testGetStudentById() {
        // First add a student
        studentService.addStudent(testStudent);
        transaction.commit();

        // Then retrieve it
        Student retrievedStudent = studentService.getStudentByStudentId(testStudent.getStudentId());

        assertNotNull(retrievedStudent);
        assertEquals(testStudent.getName(), retrievedStudent.getName());
        assertEquals(testStudent.getStudentId(), retrievedStudent.getStudentId());
    }

    @Test
    void testUpdateStudent() {
        // First add a student
        studentService.addStudent(testStudent);
        transaction.commit();

        // Start new transaction for update
        transaction = session.beginTransaction();

        // Update the student
        testStudent.setName("Updated Name");
        studentService.updateStudent(testStudent);
        transaction.commit();

        // Start new transaction for retrieval
        transaction = session.beginTransaction();

        // Retrieve the updated student
        Student updatedStudent = studentService.getStudentByStudentId(testStudent.getStudentId());

        assertEquals("Updated Name", updatedStudent.getName());
    }

    @Test
    void testDeleteStudent() {
        // First add a student
        studentService.addStudent(testStudent);
        transaction.commit();

        // Start new transaction for delete
        transaction = session.beginTransaction();

        // Delete the student
        studentService.deleteStudent(testStudent.getId());
        transaction.commit();

        // Start new transaction for retrieval
        transaction = session.beginTransaction();

        // Try to retrieve the deleted student
        Student deletedStudent = studentService.getStudentByStudentId(testStudent.getStudentId());
        assertNull(deletedStudent);
    }

    @Test
    void testGetAllStudents() {
        // Add multiple students with unique IDs
        Student student1 = new Student();
        student1.setName("Student 1");
        student1.setStudentId("STU1" + System.currentTimeMillis());
        student1.setEmail("student1@example.com");
        student1.setContactNumber("1234567891");
        student1.setRole(UserRole.STUDENT);
        studentService.addStudent(student1);

        Student student2 = new Student();
        student2.setName("Student 2");
        student2.setStudentId("STU2" + System.currentTimeMillis());
        student2.setEmail("student2@example.com");
        student2.setContactNumber("1234567892");
        student2.setRole(UserRole.STUDENT);
        studentService.addStudent(student2);

        transaction.commit();

        // Get all students
        List<Student> allStudents = studentService.getAllStudents();

        assertFalse(allStudents.isEmpty());
        assertTrue(allStudents.size() >= 2);
        assertTrue(allStudents.stream().anyMatch(s -> s.getName().equals("Student 1")));
        assertTrue(allStudents.stream().anyMatch(s -> s.getName().equals("Student 2")));
    }

    @Test
    void testGetStudentByStudentId() {
        // First add a student
        studentService.addStudent(testStudent);
        transaction.commit();

        // Then retrieve by student ID
        Student retrievedStudent = studentService.getStudentByStudentId(testStudent.getStudentId());

        assertNotNull(retrievedStudent);
        assertEquals(testStudent.getName(), retrievedStudent.getName());
        assertEquals(testStudent.getStudentId(), retrievedStudent.getStudentId());
    }
}