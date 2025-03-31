package com.library.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    private static final String URL = "jdbc:mysql://localhost:3306/library_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "1336";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load MySQL JDBC driver");
        }
    }

    public static Connection getConnection() throws SQLException {
        System.out.println("Attempting to connect to MySQL...");
        System.out.println("URL: " + URL);
        System.out.println("User: " + USER);
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("Successfully connected to MySQL!");
        return conn;
    }
}