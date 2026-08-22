# EventBooking

EventBooking is a Spring Boot backend system for an event booking platform.

## Technologies

* Java 17+
* Spring Boot
* Maven
* Spring Web
* Spring Data JPA / Hibernate
* MySQL
* Spring Security
* JWT
* MapStruct
* Lombok
* Swagger / OpenAPI
* Log4j2

## Requirements

Before setting up the project, make sure you have:

* **Java 17+**
* **Maven**
* **MySQL**
* **Git**
* **Postman** (optional)

## 1. Clone the Repository

```bash
git clone https://github.com/OrlenHyra/Event_Booking_Platform.git
cd Event_Booking
```

## 2. Configure the Database

Start MySQL and create the databases:

```sql
CREATE DATABASE event_booking_db;
CREATE DATABASE event_booking_test_db;
CREATE DATABASE event_booking_prod_db;
```

## 3. Configure Environment Variables

Create a `.env` file in the root directory:

```text
Event_Booking/
├── src/
├── .env
├── pom.xml
└── README.md
```

The `.env` file should contain:

```text
JWT_SECRET=your-generated-secret
JWT_EXPIRATION_MS=3600000

DB_USERNAME=root
DB_PASSWORD=your-database-password
```

The application uses these variables for the JWT and database configuration.

### Generate JWT Secret

A JWT secret can be generated using PowerShell:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Copy the generated value into `.env`.

The `.env` file should not be committed to Git.

Make sure `.gitignore` contains:

```text
.env
/logs
```

> Note: Spring Boot does not automatically load `.env` files. The variables must be available through the environment or the IDE run configuration.

## 4. Application Profiles

The project has separate configurations for the different environments.

### Default

```text
Database: event_booking_db
Port: 8080
```

### Test

```text
Database: event_booking_test_db
Port: 8082
```

### Production

```text
Database: event_booking_prod_db
Port: 8081
```

## 5. Install Dependencies

Run:

```bash
mvn clean install
```

## 6. Run the Application

```bash
mvn spring-boot:run
```

## 7. Authentication

Register:

```text
POST /api/auth/register
```

Login:

```text
POST /api/auth/login
```

The login endpoint returns a JWT token.

For protected endpoints, use:

```text
Authorization: Bearer <JWT_TOKEN>
```

## 8. User Roles

The application supports:

* `ADMIN`
* `ORGANIZER`
* `ATTENDEE`

Role-based authorization is handled by Spring Security.

Example:

```java
@PreAuthorize("hasRole('ADMIN')")
```

## 9. Main Features

* User registration and login
* JWT authentication
* Role-based authorization
* User management
* User activation/deactivation
* Venue management
* Category management
* Event management
* Event publishing and cancellation
* Event searching and filtering
* Event reviews and ratings
* Event bookings
* Booking cancellation
* Waitlist management
* Business rule validation
* Global exception handling
* Swagger/OpenAPI
* Log4j2 logging

## 10. Administrator

An initial administrator is created using the application's admin seeder.

The administrator can access protected administrative endpoints.

## 11. API Documentation

Swagger/OpenAPI is used for API documentation.

Swagger UI is available when the application is running.

Protected endpoints require a valid JWT token.

## 12. Logging

The application uses **Log4j2**.

Logs are stored in:

```text
logs/application.log
```

The `logs` folder is included in `.gitignore`.

## 13. Project Structure

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Main packages:

```text
config
controller
exception
model
repository
security
service
```

## 14. API Testing

Postman can be used to test the APIs.

For protected endpoints:

1. Login.
2. Copy the JWT token.
3. Add it as a Bearer token in the request.

A Postman collection can be used to save and organize the API requests.

## 15. Stopping the Application

If running with Maven:

```text
CTRL + C
```

## 16. Common Issues

**Database Connection Failure:**
Make sure MySQL is running and the database credentials are correct.

**JWT Authentication Failure:**
Make sure `JWT_SECRET` is correctly configured.

**Port Conflict:**
Change the port in the appropriate application profile.

---

**The EventBooking backend is ready to use! 🚀**
