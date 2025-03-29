package com.library.util;

import com.library.model.Book;
import com.library.service.BookService;

public class DataInitializer {
    private final BookService bookService;

    public DataInitializer() {
        this.bookService = new BookService();
    }

    public void initializeClassicBooks() {
        bookService.addClassicBooks();
    }

    public static void main(String[] args) {
        DataInitializer initializer = new DataInitializer();
        initializer.initializeClassicBooks();
        System.out.println("Classic books have been added to the database.");
    }
}