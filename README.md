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

## Project GUI
![1](https://github.com/user-attachments/assets/5f060fde-9a12-496e-a7ad-a0921c2f143f)
![mod1](https://github.com/user-attachments/assets/da90910c-c4d9-4905-91ef-7249bbe023f3)
![6](https://github.com/user-attachments/assets/f9737020-7fe9-4398-9f67-d6d53eff40cb)
![5](https://github.com/user-attachments/assets/4e64da3c-0984-42ea-b095-9d85d3c3d484)
![4](https://github.com/user-attachments/assets/84bea674-a00b-4a7d-bca7-11926ce73314)
![2](https://github.com/user-attachments/assets/f54e85b4-2278-45e1-a12b-d7456e02572e)


# System Architecture

![Screenshot 2025-05-18 112637](https://github.com/user-attachments/assets/a784ca9d-eb2f-40af-8324-cd5b83988cad)
![Screenshot 2025-05-18 112145](https://github.com/user-attachments/assets/e5ce6633-eab3-4621-bdcc-042ce7ed9cb6)
![Screenshot 2025-05-18 115017](https://github.com/user-attachments/assets/9fc83889-e011-4206-adff-f46f5a2e954b)
![Screenshot 2025-05-18 114637](https://github.com/user-attachments/assets/37b36f51-bb2e-4e1c-b506-fd7d47c88d05)

![Screenshot 2025-05-18 114422](https://github.com/user-attachments/assets/f336fc23-74c8-403e-bf37-f6ed6a477407)
![Screenshot 2025-05-18 113644](https://github.com/user-attachments/assets/4a294a89-5867-47eb-9ec3-a65cad0bbebb)
![Screenshot 2025-05-18 113418](https://github.com/user-attachments/assets/8b23484c-0d6e-447c-8183-81e66d213a2e)
![Screenshot 2025-05-18 113129](https://github.com/user-attachments/assets/48032446-b162-4b23-898c-79b4bd80e9f7)
![Screenshot 2025-05-18 112913](https://github.com/user-attachments/assets/ae059496-f9d2-4537-829a-90bd7df61b22)

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
