# Event Management System

A RESTful API for managing events, user registrations, and event organization. Built with Java 17 and Spring Boot.

## Features

- **Event Management**: Create, update, search, and manage events
- **User Management**: User registration, profile management, and role-based permissions
- **Registration System**: Register for events, view registrations, and manage capacity
- **Security**: JWT authentication and authorization with role-based access control
- **API Documentation**: Swagger/OpenAPI documentation

## Technologies Used

- Java 17
- Spring Boot 3.0
- Spring Data JPA
- Spring Security
- JWT Authentication
- MySQL Database
- Swagger/OpenAPI
- JUnit/Mockito

## API Endpoints

### Authentication
- `POST /api/auth/login` - Authenticate user and get JWT token
- `POST /api/auth/register` - Register a new user

### Events
- `GET /api/events` - Get all events (or published events only with query param)
- `GET /api/events/{id}` - Get a specific event by ID
- `POST /api/events` - Create a new event (ADMIN, ORGANIZER)
- `PUT /api/events/{id}` - Update an event (ADMIN, ORGANIZER)
- `DELETE /api/events/{id}` - Delete an event (ADMIN, ORGANIZER)
- `GET /api/events/search` - Search events by keyword, category, and date
- `GET /api/events/organizer/{organizerId}` - Get events by organizer
- `PATCH /api/events/{id}/publish` - Publish an event (ADMIN, ORGANIZER)
- `PATCH /api/events/{id}/unpublish` - Unpublish an event (ADMIN, ORGANIZER)

### Users
- `GET /api/users` - Get all users (ADMIN)
- `GET /api/users/{id}` - Get a specific user by ID
- `GET /api/users/username/{username}` - Get a user by username
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user

### Registrations
- `GET /api/registrations` - Get all registrations (ADMIN)
- `GET /api/registrations/{id}` - Get a specific registration by ID
- `GET /api/registrations/user/{userId}` - Get registrations by user
- `GET /api/registrations/event/{eventId}` - Get registrations by event
- `POST /api/registrations/user/{userId}/event/{eventId}` - Register a user for an event
- `PATCH /api/registrations/user/{userId}/event/{eventId}/cancel` - Cancel a registration
- `DELETE /api/registrations/{id}` - Delete a registration (ADMIN)

## Setting Up the Project

### Prerequisites
- Java 17+
- Maven
- MySQL

### Configuration
1. Clone the repository
2. Configure your MySQL database in `application.properties`
3. Run the application: 

```
mvn spring-boot:run
```

### Default Users
The application initializes with the following test users:
- Admin: username=`admin`, password=`admin123`
- Organizer: username=`organizer`, password=`organizer123`
- Regular User: username=`user`, password=`user123`

## Environment Setup

This project uses environment variables for configuration. Follow these steps to set up:

1. Copy the `.env-template` file to a new file named `.env`
2. Fill in your own values in the `.env` file
3. DO NOT commit the `.env` file to version control (it should be ignored by `.gitignore`)

Example:
```
cp .env-template .env
```

## API Documentation
When the application is running, the Swagger UI is available at:
[SwaggerUI](http://localhost:8080/swagger-ui/index.html)





