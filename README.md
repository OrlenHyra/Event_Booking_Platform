# EventBooking

EventBooking is a Spring Boot backend system for an event booking platform.

## Technologies

* Java
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

## Current Features

The following functionality has currently been implemented:

* User registration
* User login
* JWT-based authentication
* Role-based authorization
* ADMIN role management
* User management
* Venue management

## Roles

The application currently supports three user roles:

* `ADMIN`
* `ORGANIZER`
* `ATTENDEE`

Administrative endpoints are protected using Spring Security and role-based authorization.

For example:

```text
@PreAuthorize("hasRole('ADMIN')")
```

## Requirements

Before running the application, make sure you have:

* Java installed (17 or above)
* Maven installed
* MySQL installed and running
* Git installed

## Database Setup

Create a MySQL database for the application.

Example:

```sql
CREATE DATABASE event_booking_db;
```

Database connection details are provided through the application's configuration.

## Environment Variables

Sensitive configuration is kept outside the source code.

The JWT secret is provided through an environment variable:

```text
JWT_SECRET=your-generated-secret
```

The application reads the secret through the Spring configuration:

```text
jwt.secret=${JWT_SECRET}
```

### Creating the JWT Secret

A secure JWT secret can be generated using PowerShell:

```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

Copy the generated value and add it to the `.env` file.

The `.env` file should be located in the root directory of the project.

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
```

The real JWT secret should **never be committed to the Git repository**.

Make sure `.env` is included in `.gitignore`:

```text
.env
```

> Note: Spring Boot does not automatically load `.env` files by default. The `JWT_SECRET` environment variable must be available to the application through the environment or the IDE's run configuration.

## Running the Application

Clone the repository:

```bash
git clone https://github.com/OrlenHyra/Event_Booking_Platform.git
```

Navigate into the project:

```bash
cd Event_Booking
```

Build the project:

```bash
mvn clean install
```

Run the application:

```bash
mvn spring-boot:run
```

## Authentication

Unauthenticated users can register through the authentication API:

```text
POST /api/auth/register
```

Users can log in through:

```text
POST /api/auth/login
```

A successful login returns a JWT token.

The token must be included when accessing protected endpoints:

```text
Authorization: Bearer <JWT_TOKEN>
```

## Administrator

An initial administrator can be created automatically using the application's admin seeder.

The administrator can access protected administrative functionality, including user and venue management.

## API Documentation

Swagger/OpenAPI is used to document the API.

Once the application is running, Swagger UI can be accessed through the configured Swagger endpoint.

Protected endpoints require a valid JWT token.

## Project Structure

The application follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

DTOs and mappers are used to separate API models from JPA entities.

The main package structure includes:

```text
config
controller
exception
model
repository
security
service
```

## Current Development Status

The project is currently under development.

Implemented modules will be expanded with the remaining EventBooking functionality, including events, bookings, reviews, advanced business rules, testing, logging, and environment profiles.
