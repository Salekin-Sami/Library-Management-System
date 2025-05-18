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
- **Personalized Book Recommendation System**: Students receive up to 5 book recommendations on their dashboard, combining:
  - Books from their favorite categories (based on borrowing history)
  - New arrivals (recently added books)
  - Popular books (most borrowed by all students)
  - Recommendations are unique and tailored for each student

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

## Project GUI
![2](https://github.com/user-attachments/assets/1dbcf615-4603-4276-a577-190573fd54c4)
![1](https://github.com/user-attachments/assets/8f60d61b-6d1d-4e6b-a56b-04eadd6275e5)
![mod1](https://github.com/user-attachments/assets/d8050094-5bc5-4cfa-9e05-1f62d4c59078)
![6](https://github.com/user-attachments/assets/8369c27b-fab9-467d-99b9-87958a8aec31)
![5](https://github.com/user-attachments/assets/05c2c188-363e-4114-81e6-df743e70dea4)
![4](https://github.com/user-attachments/assets/ee48195b-cb75-4124-88f2-4553125de5c1)

# System Architecture
![Screenshot 2025-05-18 112913](https://github.com/user-attachments/assets/9f1651d2-a08a-4cc9-b502-0ec5c96eb837)
![Screenshot 2025-05-18 112637](https://github.com/user-attachments/assets/74bc16f0-6fa3-4827-b568-10225468e489)
![Screenshot 2025-05-18 112145](https://github.com/user-attachments/assets/300c9d94-2418-4c75-8b30-bb844578229e)
![Screenshot 2025-05-18 115017](https://github.com/user-attachments/assets/4b701568-8b66-401b-8880-2db44146571d)
![Screenshot 2025-05-18 114637](https://github.com/user-attachments/assets/f995395f-b9a9-424b-937d-f33c561d2c38)
![Screenshot 2025-05-18 114422](https://github.com/user-attachments/assets/8a3c0c18-e959-4cb4-a580-b93b74816cee)
![Screenshot 2025-05-18 113644](https://github.com/user-attachments/assets/b3f6e5a2-6b6b-4673-b9aa-9722ad2a2098)
![Screenshot 2025-05-18 113418](https://github.com/user-attachments/assets/ba949d10-e5fd-400e-afe7-27da6e95437a)
![Screenshot 2025-05-18 113129](https://github.com/user-attachments/assets/df841e03-8897-4162-80fc-fe6cd81aab95)


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
