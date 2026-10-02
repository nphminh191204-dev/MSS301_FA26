# FU Cinema Booking System — MSS301 Assignment 1

## Architecture
```
Client → API Gateway (9000) → customer-service (8081) [MSSQL]
                             → movie-service    (8082) [MongoDB]
                             → booking-service  (8083) [MySQL]
```

## Prerequisites
- Java 21, Maven, Docker Desktop

## Quick Start
```bash
# 1. Start databases
docker compose up -d

# 2. Start each service in separate terminals
cd customer-service && mvn spring-boot:run
cd movie-service    && mvn spring-boot:run
cd booking-service  && mvn spring-boot:run
cd api-gateway      && mvn spring-boot:run
```

## Default Credentials
| Account | Email | Password |
|---------|-------|----------|
| Admin | admin@fucinema.com | @@abc123@@  |
| Customer (An) | an@gmail.com | 123456 |
| Customer (Bình) | binh@gmail.com | 123456 |
| Customer (Chi - INACTIVE) | chi@gmail.com | 123456 |

## API Endpoints (via Gateway :9000)
### Auth
- `POST /api/auth/login` — Login, get JWT

### Customers
- `POST /api/customers/register` — Public register
- `GET  /api/customers/me` — Customer: get profile
- `PUT  /api/customers/me` — Customer: update profile
- `PUT  /api/customers/me/password` — Customer: change password
- `GET  /api/customers` — Admin: list/search
- `GET  /api/customers/{id}` — Admin: get by ID
- `POST /api/customers` — Admin: create
- `PUT  /api/customers/{id}` — Admin: update
- `DELETE /api/customers/{id}` — Admin: deactivate (soft)

### Genres
- `GET  /api/genres` — Public list
- `GET  /api/genres/{id}` — Public detail
- `POST /api/genres` — Admin create
- `PUT  /api/genres/{id}` — Admin update
- `DELETE /api/genres/{id}` — Admin delete

### Rooms
- `GET  /api/rooms` — Admin list
- `POST /api/rooms` — Admin create
- `PUT  /api/rooms/{id}` — Admin update
- `DELETE /api/rooms/{id}` — Admin delete

### Movies
- `GET  /api/movies` — Public list (filter: keyword, genreId, status)
- `GET  /api/movies/{id}` — Public detail
- `POST /api/movies` — Admin create
- `PUT  /api/movies/{id}` — Admin update
- `DELETE /api/movies/{id}` — Admin soft-delete (→ENDED)

### Showtimes
- `GET  /api/showtimes` — Public list (filter: movieId, date)
- `GET  /api/showtimes/{id}` — Public detail
- `POST /api/showtimes` — Admin create
- `PUT  /api/showtimes/{id}` — Admin update
- `DELETE /api/showtimes/{id}` — Admin cancel (soft)

### Bookings
- `GET  /api/bookings/showtimes/{id}/seats` — Public seat map
- `POST /api/bookings` — Customer create booking
- `GET  /api/bookings/my` — Customer booking history
- `GET  /api/bookings/{id}` — Owner or Admin get detail
- `PUT  /api/bookings/{id}/cancel` — Owner (2h+ before) or Admin cancel
- `GET  /api/bookings` — Admin list all
- `GET  /api/bookings/report?startDate=&endDate=` — Admin revenue report

## Testing
```powershell
# Run integration tests
powershell -ExecutionPolicy Bypass -File test_all.ps1

# Import Postman collection
# File: postman/FUCinemaBookingSystem.postman_collection.json
# Env:  postman/FUCinema-Local.postman_environment.json
```

## Business Rules Implemented
- BR01: Email unique across all customers + admin
- BR02: INACTIVE customers cannot login (403)
- BR03: Customer can only access own profile
- BR04: Cannot schedule ENDED movie
- BR05: No overlapping showtimes in same room
- BR06: Showtime soft-delete (→CANCELLED)
- BR07: No duplicate seats in same booking request (400)
- BR08: Showtime must be SCHEDULED to book
- BR09: Seat already taken → 409 Conflict
- BR10: Price computed server-side from showtime.ticketPrice
- BR11: Customer can only access own bookings
- BR12: Customer cannot cancel < 2h before showtime
- BR13: Report startDate must be ≤ endDate
- BR14: Showtime start must be in the future
- BR15: Movie/Room must exist to create showtime
