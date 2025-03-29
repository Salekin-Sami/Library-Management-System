# Library Management System

A JavaFX-based library management system that helps librarians manage books, students, and borrowing operations efficiently.

## Features

- Book Management
  - Add, edit, and delete books
  - Track multiple copies of books
  - Search books by title, author, or ISBN
  - View book details and borrowing history

- Student Management
  - Add, edit, and delete students
  - View student details and borrowing history
  - Track student fines and payments

- Borrowing Management
  - Issue books to students
  - Return books with fine calculation
  - Track overdue books
  - View active borrowings and history

## Technologies Used

- Java 17
- JavaFX 17
- Hibernate
- MySQL
- Maven

## Prerequisites

- JDK 17 or later
- Maven 3.6 or later
- MySQL 8.0 or later

## Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/library-management-system.git
```

2. Create a MySQL database named `library_db`

3. Update the database configuration in `src/main/resources/hibernate.cfg.xml`

4. Build the project:
```bash
mvn clean install
```

5. Run the application:
```bash
mvn javafx:run
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details. 