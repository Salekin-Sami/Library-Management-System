package com.library.model;

import java.time.LocalDateTime;

public class StudentProfile {
    private int userId;
    private String fullName;
    private String studentId;
    private String phoneNumber;
    private String email;
    private int booksBorrowed;
    private int maxBooks;

    public StudentProfile() {
    }

    public StudentProfile(int userId, String fullName, String studentId, String phoneNumber,
            String email, int booksBorrowed, int maxBooks) {
        this.userId = userId;
        this.fullName = fullName;
        this.studentId = studentId;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.booksBorrowed = booksBorrowed;
        this.maxBooks = maxBooks;
    }

    // Getters and Setters

    /**
     * Returns the user ID of the student.
     * 
     * @return the user ID
     */
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getBooksBorrowed() {
        return booksBorrowed;
    }

    public void setBooksBorrowed(int booksBorrowed) {
        this.booksBorrowed = booksBorrowed;
    }

    public int getMaxBooks() {
        return maxBooks;
    }

    public void setMaxBooks(int maxBooks) {
        this.maxBooks = maxBooks;
    }

    public boolean canBorrowMore() {
        return booksBorrowed < maxBooks;
    }
}