# Library Management System

A JavaFX-based library management system that helps librarians manage books, students, and borrowing operations efficiently.

## Features

### Book Management
- Add, edit, and delete books
- Track multiple copies of books with individual status tracking
- Search books by title, author, or ISBN
- View detailed book information including:
  - Cover image
  - Publication details
  - Edition information
  - Rating system
  - Description
  - Copy locations and status
  - Borrowing history
- Add new copies to existing books
- Track book availability status

### Student Management
- Add, edit, and delete students
- View student details and borrowing history
- Track student fines and payments
- Student profile management
- Student dashboard with personalized view
- View student borrowing history with detailed information

### Borrowing Management
- Issue books to students
- Return books with automatic fine calculation
- Track overdue books
- View active borrowings and history
- Automatic fine calculation for overdue books
- Book request system for students
- Track borrowing status and history

### User Interface
- Modern and intuitive JavaFX interface
- Dark/Light theme support
- Responsive design
- Search functionality across all modules
- Detailed views for books and students
- Statistics dashboard showing:
  - Total books
  - Total students
  - Current borrowings
  - Overdue books
  - Total unpaid fines

### User Roles
- Administrator (Librarian) access with full system control
- Student access with limited functionality
- Secure login system
- Remember me functionality
- Role-based access control

### Additional Features
- Book request system
- Fine management system
- Statistics and reporting
- User guide and help system
- About section with system information
- Confirmation dialogs for important actions
- Error handling and user notifications

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