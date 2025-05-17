package com.library.service;

import com.library.model.Book;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BookApiService {
    private final OkHttpClient client;
    private static final String GOOGLE_BOOKS_API_URL = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
    private static final String GOOGLE_BOOKS_SEARCH_URL = "https://www.googleapis.com/books/v1/volumes?q=";

    public BookApiService() {
        this.client = new OkHttpClient();
    }

    /**
     * Searches for books using the Google Books API.
     * 
     * @param query The search query.
     * @return A list of books matching the search query.
     * @throws IOException If there is an error connecting to the API.
     */
    public List<Book> searchBooks(String query) throws IOException {
        List<Book> books = new ArrayList<>();
        String url = GOOGLE_BOOKS_SEARCH_URL + query.replace(" ", "+");
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                JSONObject jsonObject = new JSONObject(jsonResponse);

                // Check if the response contains "items"
                if (jsonObject.has("items")) {
                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject volumeInfo = items.getJSONObject(i).getJSONObject("volumeInfo");
                        Book book = new Book();

                        // Set title
                        book.setTitle(volumeInfo.optString("title", "Unknown Title"));

                        // Set author(s)
                        if (volumeInfo.has("authors")) {
                            JSONArray authors = volumeInfo.getJSONArray("authors");
                            StringBuilder authorNames = new StringBuilder();
                            for (int j = 0; j < authors.length(); j++) {
                                if (j > 0)
                                    authorNames.append(", ");
                                authorNames.append(authors.getString(j));
                            }
                            book.setAuthor(authorNames.toString());
                        } else {
                            book.setAuthor("Unknown Author");
                        }

                        // Set ISBN
                        if (volumeInfo.has("industryIdentifiers")) {
                            JSONArray identifiers = volumeInfo.getJSONArray("industryIdentifiers");
                            String isbn = "";
                            for (int j = 0; j < identifiers.length(); j++) {
                                JSONObject identifier = identifiers.getJSONObject(j);
                                String type = identifier.getString("type");
                                if (type.equals("ISBN_13")) {
                                    isbn = identifier.getString("identifier");
                                    break;
                                } else if (type.equals("ISBN_10") && isbn.isEmpty()) {
                                    isbn = identifier.getString("identifier");
                                }
                            }
                            book.setIsbn(isbn.isEmpty() ? "Unknown ISBN" : isbn);
                        } else {
                            book.setIsbn("Unknown ISBN");
                        }

                        // Set other fields
                        book.setPublisher(volumeInfo.optString("publisher", "Unknown Publisher"));
                        book.setPublicationYear(volumeInfo.optString("publishedDate", "Unknown Year").split("-")[0]);
                        book.setDescription(volumeInfo.optString("description", "No description available"));
                        book.setCategory(
                                volumeInfo.has("categories") ? volumeInfo.getJSONArray("categories").getString(0)
                                        : "Uncategorized");

                        // Set cover image URL
                        if (volumeInfo.has("imageLinks")) {
                            book.setCoverImageUrl(volumeInfo.getJSONObject("imageLinks").optString("thumbnail"));
                        }

                        // Set rating
                        if (volumeInfo.has("averageRating")) {
                            book.setRating(volumeInfo.getDouble("averageRating"));
                        }

                        books.add(book);
                    }
                }
            }
        }
        return books;
    }

    public void fetchBookDetails(Book book) {
        try {
            String url = GOOGLE_BOOKS_API_URL + book.getIsbn();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    JSONObject jsonObject = new JSONObject(jsonResponse);

                    if (jsonObject.has("items") && jsonObject.getJSONArray("items").length() > 0) {
                        JSONObject volumeInfo = jsonObject.getJSONArray("items")
                                .getJSONObject(0)
                                .getJSONObject("volumeInfo");

                        // Update book details if available
                        if (volumeInfo.has("imageLinks")) {
                            String coverUrl = volumeInfo.getJSONObject("imageLinks")
                                    .getString("thumbnail");
                            book.setCoverImageUrl(coverUrl);
                        }

                        if (volumeInfo.has("averageRating")) {
                            double rating = volumeInfo.getDouble("averageRating");
                            book.setRating(rating);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}