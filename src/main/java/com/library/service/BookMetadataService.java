package com.library.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.library.model.Book;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class BookMetadataService {
    private static final String GOOGLE_BOOKS_API = "https://www.googleapis.com/books/v1/volumes?q=";
    private static final String OPEN_LIBRARY_API = "https://openlibrary.org/search.json?q=";
    private final Gson gson;

    public BookMetadataService() {
        this.gson = new Gson();
    }

    public ObservableList<Book> searchBooks(String query) {
        ObservableList<Book> results = FXCollections.observableArrayList();

        try {
            // Search Google Books API
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String googleBooksUrl = GOOGLE_BOOKS_API + encodedQuery;
            List<Book> googleBooks = searchGoogleBooks(googleBooksUrl);
            results.addAll(googleBooks);

            // Search Open Library API
            String openLibraryUrl = OPEN_LIBRARY_API + encodedQuery;
            List<Book> openLibraryBooks = searchOpenLibrary(openLibraryUrl);
            results.addAll(openLibraryBooks);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private List<Book> searchGoogleBooks(String urlString) throws Exception {
        List<Book> books = new ArrayList<>();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonObject response = gson.fromJson(br, JsonObject.class);
            JsonArray items = response.getAsJsonArray("items");

            if (items != null) {
                for (JsonElement item : items) {
                    JsonObject volumeInfo = item.getAsJsonObject().getAsJsonObject("volumeInfo");
                    Book book = new Book();

                    // Set basic information
                    book.setTitle(getStringValue(volumeInfo, "title"));
                    book.setAuthor(getStringValue(volumeInfo, "authors", 0));
                    book.setIsbn(getStringValue(volumeInfo, "industryIdentifiers", 0, "identifier"));
                    book.setEdition(getStringValue(volumeInfo, "edition"));
                    book.setPublisher(getStringValue(volumeInfo, "publisher"));
                    book.setPublicationYear(getStringValue(volumeInfo, "publishedDate", 0, 4));
                    book.setCategory(getStringValue(volumeInfo, "categories", 0));
                    book.setDescription(getStringValue(volumeInfo, "description"));

                    books.add(book);
                }
            }
        }
        conn.disconnect();
        return books;
    }

    private List<Book> searchOpenLibrary(String urlString) throws Exception {
        List<Book> books = new ArrayList<>();
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        if (conn.getResponseCode() == 200) {
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            JsonObject response = gson.fromJson(br, JsonObject.class);
            JsonArray docs = response.getAsJsonArray("docs");

            if (docs != null) {
                for (JsonElement doc : docs) {
                    JsonObject bookInfo = doc.getAsJsonObject();
                    Book book = new Book();

                    // Set basic information
                    book.setTitle(getStringValue(bookInfo, "title"));
                    book.setAuthor(getStringValue(bookInfo, "author_name", 0));
                    book.setIsbn(getStringValue(bookInfo, "isbn", 0));
                    book.setEdition(getStringValue(bookInfo, "edition_count"));
                    book.setPublisher(getStringValue(bookInfo, "publisher", 0));
                    book.setPublicationYear(getStringValue(bookInfo, "publish_year", 0));
                    book.setCategory(getStringValue(bookInfo, "subject", 0));

                    books.add(book);
                }
            }
        }
        conn.disconnect();
        return books;
    }

    private String getStringValue(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        return element != null ? element.getAsString() : "";
    }

    private String getStringValue(JsonObject obj, String key, int arrayIndex) {
        JsonElement element = obj.get(key);
        if (element != null && element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() > arrayIndex) {
                return array.get(arrayIndex).getAsString();
            }
        }
        return "";
    }

    private String getStringValue(JsonObject obj, String key, int arrayIndex, String nestedKey) {
        JsonElement element = obj.get(key);
        if (element != null && element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            if (array.size() > arrayIndex) {
                JsonObject nestedObj = array.get(arrayIndex).getAsJsonObject();
                JsonElement nestedElement = nestedObj.get(nestedKey);
                return nestedElement != null ? nestedElement.getAsString() : "";
            }
        }
        return "";
    }

    private String getStringValue(JsonObject obj, String key, int startIndex, int length) {
        String value = getStringValue(obj, key);
        if (value.length() >= startIndex + length) {
            return value.substring(startIndex, startIndex + length);
        }
        return "";
    }
}