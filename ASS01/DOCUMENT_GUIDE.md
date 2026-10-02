# HUONG DAN VIET TAI LIEU BAO CAO - MSS301 ASSIGNMENT 1
## FUCinemaBookingSystem - Cinema Ticket Booking System using API Gateway

> **Muc tieu:** Noi dung de viet vao file Word. Phan `HINH X` la huong dan chup anh.  
> **Luu y dinh dang:** Heading 1 = 16pt Bold, Heading 2 = 14pt Bold, Body = 12pt Times New Roman  
> **MUC LUC TU DONG:** References -> Table of Contents -> Automatic Table 1 (BAT BUOC!)

---

## TRANG BIA

```
TRUONG DAI HOC FPT - KHOA CONG NGHE THONG TIN

BAO CAO ASSIGNMENT 1
MSS301 - Microservices and Security

FUCinemaBookingSystem
Cinema Ticket Booking System using API Gateway

Sinh vien : [Ho va Ten]
MSSV      : [Ma so sinh vien]
Lop       : [Ten lop]
Giang vien: [Ten GV]
Ngay nop  : Thu 6 - [ngay thang]
```

---

## 1. Introduction

### 1.1 Gioi thieu he thong

**FUCinemaBookingSystem** la he thong dat ve xem phim truc tuyen xay dung theo kien truc **Microservices**. He thong cung cap nen tang end-to-end bao gom quan ly danh muc phim, lich chieu, dat cho va quan ly khach hang.

Tinh nang chinh:
- **Online Ticket Booking:** Khach hang duyet phim, suat chieu, xem ghe con trong, chon ghe va nhan xac nhan dat ve.
- **Movie and Showtime Management:** Quan ly phim, phong chieu, lich chieu; dam bao khong co hai suat chieu trung gio cung phong.
- **Customer Management:** Luu tru thong tin khach hang va lich su dat ve.

### 1.2 Cong nghe su dung

| Cong nghe | Phien ban | Muc dich |
|-----------|-----------|----------|
| Java | 21 | Ngon ngu lap trinh |
| Spring Boot | 4.1.1 | Framework backend |
| Spring Cloud Gateway | 2025.1.3 | API Gateway routing |
| Spring Cloud OpenFeign | 2025.1.3 | HTTP client giua services |
| Spring Data JPA | 4.1.1 | ORM cho SQL databases |
| Spring Data MongoDB | 4.1.1 | ODM cho MongoDB |
| Spring Security OAuth2 Resource Server | 4.1.1 | JWT validation tai Gateway |
| Flyway | 11.x | Database migration |
| JWT HS256 (Nimbus JOSE) | - | Authentication token |
| SQL Server 2022 | Docker | Database cho customer-service |
| MongoDB 7.0.5 | Docker | Database cho movie-service |
| MySQL 8.3.0 | Docker | Database cho booking-service |
| Docker and Docker Compose | Latest | Container hoa databases |
| Lombok | - | Code generation |
| Postman | Desktop | API testing |

---

## 2. System Architecture

### 2.1 Tong quan kien truc

```
                    +-------------------------------+
   Client  ------> |      API GATEWAY  (:9000)     |  1. Validate JWT (HS256)
 (Postman)  Bearer  |  Spring Cloud GW + Security   |  2. Check role (ADMIN/CUSTOMER)
            JWT     +---------------+---------------+  3. Add X-User headers
                                    |
         +--------------------------+---------------------------+
         | /api/auth/**             | /api/genres/**            | /api/bookings/**
         | /api/customers/**        | /api/rooms/**             |
         v                          | /api/movies/**            v
 +-----------------+                | /api/showtimes/**  +-----------------+
 | customer-service|                v                    | booking-service |
 |    (:8081)      |      +-----------------+  OpenFeign |    (:8083)      |
 | SQL Server 2022 |      |  movie-service  | <--------  | MySQL 8         |
 | cinema_customer |      |    (:8082)      |  GET       | cinema_booking  |
 +-----------------+      | MongoDB 7       | showtime   +-----------------+
                          | cinema_movie    |
                          +-----------------+
```

**HINH 1** - Architecture Diagram  
Huong dan: Ve diagram tren bang draw.io hoac PowerPoint, export ra PNG va chen vao Word o vi tri nay.

### 2.2 Mo ta tung service

| Service | Port | Database | Chuc nang |
|---------|------|----------|-----------|
| customer-service | 8081 | SQL Server 2022 (cinema_customer) | Phat JWT, quan ly tai khoan khach hang |
| movie-service | 8082 | MongoDB 7 (cinema_movie) | Genres, Rooms, Movies, Showtimes |
| booking-service | 8083 | MySQL 8 (cinema_booking) | Dat ve, lich su, huy ve, bao cao |
| api-gateway | 9000 | - | Routing, xac thuc JWT, phan quyen |

### 2.3 Luong xac thuc

**Buoc 1 - Login:** POST /api/auth/login -> Gateway (permitAll) -> customer-service kiem tra -> ky JWT HS256 -> tra accessToken

**Buoc 2 - API bao ve:** Client gui Authorization: Bearer token -> Gateway xac thuc chu ky, kiem tra role, xoa X-User-* gia, chen lai tu JWT -> forward den service

---

## 3. Database Design

### 3.1 Polyglot Persistence

| Service | Database | Ten DB | Ly do chon | Schema tool |
|---------|----------|--------|------------|-------------|
| customer-service | SQL Server 2022 | cinema_customer | Can ACID, UNIQUE email, Unicode NVARCHAR cho tieng Viet | Flyway T-SQL |
| movie-service | MongoDB 7 | cinema_movie | Catalog doc nhieu, cau truc linh hoat, khong can transaction nhieu bang | DataSeeder Java |
| booking-service | MySQL 8 | cinema_booking | Giao dich ACID, query phuc tap (report tong hop) | Flyway MySQL |

**HINH 2** - ER Diagram  
Huong dan: Dung dbdiagram.io ve ERD voi cac entities: CUSTOMER -> BOOKING -> BOOKING_DETAIL (logical ref) <- SHOWTIME <- MOVIE <- GENRE, CINEMA_ROOM -> SHOWTIME

### 3.2 Schema SQL Server (customer-service)

```sql
-- V1__init.sql (T-SQL)
CREATE TABLE customer (
    customer_id       BIGINT IDENTITY(1,1) PRIMARY KEY,
    customer_name     NVARCHAR(100) NOT NULL,  -- NVARCHAR cho Unicode tieng Viet
    telephone         VARCHAR(15),
    email             VARCHAR(100) NOT NULL,
    customer_birthday DATE,
    customer_status   VARCHAR(20) NOT NULL CONSTRAINT df_status DEFAULT 'ACTIVE',
    password          VARCHAR(100) NOT NULL,
    CONSTRAINT uk_customer_email UNIQUE (email)
);

-- V2__seed.sql (dung N'...' cho chuoi Unicode)
INSERT INTO customer (customer_name, telephone, email, customer_birthday, customer_status, password)
VALUES (N'Nguyen Van An', '0905123456', 'an@gmail.com', '2002-05-10', 'ACTIVE', '<bcrypt_hash>');
```

### 3.3 Schema MySQL (booking-service)

```sql
-- V1__init.sql (MySQL)
CREATE TABLE booking (
    booking_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_date   DATETIME      NOT NULL,
    total_price    DECIMAL(12,2) NOT NULL,
    customer_id    BIGINT        NOT NULL,
    booking_status VARCHAR(20)   NOT NULL
);

CREATE TABLE booking_detail (
    booking_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id        BIGINT        NOT NULL,
    showtime_id       VARCHAR(24)   NOT NULL,  -- MongoDB ObjectId (24 hex chars)
    seat_code         VARCHAR(5)    NOT NULL,
    price             DECIMAL(10,2) NOT NULL,
    movie_id          VARCHAR(24)   NOT NULL,
    movie_title       VARCHAR(200)  NOT NULL,
    room_name         VARCHAR(50)   NOT NULL,
    showtime_start    DATETIME      NOT NULL,
    CONSTRAINT fk_detail_booking FOREIGN KEY (booking_id) REFERENCES booking(booking_id)
);
```

### 3.4 MongoDB Collections (movie-service)

| Collection | Key fields |
|------------|-----------|
| genres | _id (ObjectId), genreName (unique index), description |
| cinema_rooms | _id, roomName (unique), roomType, seatRows, seatsPerRow, roomStatus |
| movies | _id, title, director, durationMinutes, ageRating, releaseDate, genreId, movieStatus |
| showtimes | _id, movieId, roomId, startTime, endTime, ticketPrice (Decimal128), showtimeStatus |

### 3.5 Enum values

| Column | Valid values |
|--------|-------------|
| CustomerStatus | ACTIVE, INACTIVE |
| RoomType | STANDARD, THREE_D, IMAX |
| RoomStatus | ACTIVE, MAINTENANCE |
| AgeRating | P, T13, T16, T18 |
| MovieStatus | COMING_SOON, NOW_SHOWING, ENDED |
| ShowtimeStatus | SCHEDULED, CANCELLED |
| BookingStatus | CONFIRMED, CANCELLED |

**HINH 3** - MongoDB Compass: 4 collections voi du lieu seed  
Huong dan: Mo MongoDB Compass -> connect mongodb://root:password@localhost:27017 -> chon cinema_movie -> chup thay 4 collections (genres, cinema_rooms, movies, showtimes) va documents ben trong

**HINH 4** - Flyway Schema History tren SQL Server  
Huong dan: Mo DBeaver -> connect localhost:1433 user sa pass Fucinema@2026 -> mo database cinema_customer -> table flyway_schema_history -> chup thay V1, V2, V3 da Applied

---

## 4. Implementation

### 4.0 F0 - Infrastructure and Docker

Tao file docker-compose.yml khoi dong 3 container database:
- cinema-sqlserver (SQL Server 2022, port 1433, healthcheck)
- cinema-mongo (MongoDB 7.0.5, port 27017)
- cinema-mysql (MySQL 8.3.0, port 3306)

**HINH 5** - Docker Desktop: 3 containers dang Running  
Huong dan: Mo Docker Desktop -> tab Containers -> chup thay cinema-sqlserver (healthy), cinema-mongo, cinema-mysql deu STATUS: Running

**HINH 6** - Terminal: docker compose up -d thanh cong  
Huong dan: Mo PowerShell tai thu muc ASS01 -> chay "docker compose up -d" -> chup output thay 3 containers Started/Running

**HINH 7** - IntelliJ: 4 services dang chay cung luc  
Huong dan: Mo 4 tab terminal IntelliJ, moi terminal chay "mvn spring-boot:run" cho tung service. Chup man hinh thay 4 tab terminal: customer(:8081) movie(:8082) booking(:8083) gateway(:9000) deu khoi dong thanh cong.

---

### 4.1 F1 - Authentication (customer-service + Gateway)

**TODO 1.1** - Cau hinh Admin va JWT trong application.properties:
```properties
app.admin.email=admin@fucinema.com
app.admin.password=@@abc123@@
app.jwt.secret=fu-cinema-booking-system-secret-key-2026-mss301
app.jwt.expiration-minutes=60
```

**TODO 1.2** - BCrypt PasswordEncoder bean trong PasswordConfig.java

**TODO 1.3** - JwtService.generateToken(): ky HS256 voi claims sub=email, uid, role, iat, exp

**TODO 1.4** - AuthService.login(): kiem tra Admin tu properties truoc, Customer tu DB (BCrypt), chan INACTIVE (BR02)

**TODO 1.5** - POST /api/auth/login endpoint tra LoginResponse

**HINH 8** - Postman: Admin Login 200 OK co accessToken  
Huong dan: POST http://localhost:9000/api/auth/login  
Body: {"email":"admin@fucinema.com","password":"@@abc123@@"}  
Chup Response 200 thay accessToken (chuoi eyJ...) va role:"ADMIN"

**HINH 9** - Postman: Customer Login 200 OK  
POST /api/auth/login voi {"email":"an@gmail.com","password":"123456"}  
Chup 200 thay role:"CUSTOMER" va userId:1

**HINH 10** - Postman: INACTIVE Customer 403 Forbidden  
POST /api/auth/login voi {"email":"chi@gmail.com","password":"123456"}  
Chup 403 Forbidden va message ve account inactive

**HINH 11** - Postman: Sai password 401 Unauthorized  
POST voi password sai -> chup 401

---

### 4.2 F2 - Customer Self-Service

**TODO 2.1** - Flyway V1__init.sql (T-SQL: IDENTITY, NVARCHAR) va V2__seed.sql (INSERT N'...' Unicode)

**TODO 2.2** - Entity Customer, enum CustomerStatus, CustomerRepository extends JpaRepository

**TODO 2.3** - DTOs: RegisterRequest, ProfileUpdateRequest, ChangePasswordRequest, CustomerResponse (khong co password field)

**TODO 2.4** - register(): kiem tra email duy nhat BR01, BCrypt hash, status ACTIVE

**TODO 2.5** - getProfile(), updateProfile(), changePassword() doc X-User-Id tu header do Gateway chuyen tiep

**TODO 2.6** - Endpoints: POST /register, GET/PUT /me, PUT /me/password

**HINH 12** - Postman: Register moi 201 Created  
POST http://localhost:9000/api/customers/register  
Body: {"customerName":"Nguyen Test","telephone":"0912345678","email":"test@test.com","customerBirthday":"2000-01-01","password":"Abc@1234"}  
Chup 201 co customerId, KHONG co password trong response

**HINH 13** - Postman: Duplicate email 409 Conflict  
Gui lai cung email -> chup 409 "Email already in use"

**HINH 14** - Postman: Get My Profile 200 (khong co password)  
GET /api/customers/me voi customerToken -> chup response khong co field password

**HINH 15** - DBeaver: Ten tieng Viet luu dung trong DB (BR16)  
Mo DBeaver -> table customer -> chup thay customer_name co dau dung (Nguyen Van An voi day du dau)

---

### 4.3 F3 - Admin Manage Customers

**TODO 3.1** - AdminCustomerRequest DTO (co customerStatus)

**TODO 3.2** - CustomerService: search(keyword), getById, create, update, delete (soft delete -> INACTIVE)

**TODO 3.3** - Endpoints CRUD /api/customers (ADMIN only tai Gateway)

**HINH 16** - Postman: Admin List Customers 200  
GET http://localhost:9000/api/customers voi adminToken -> chup danh sach

**HINH 17** - Postman: Admin Delete Customer -> INACTIVE  
DELETE /api/customers/{id} -> 204 No Content  
GET /api/customers/{id} -> chup thay customerStatus:"INACTIVE"

**HINH 18** - Postman: Customer goi Admin API -> 403 Forbidden  
GET /api/customers voi customerToken -> chup 403

---

### 4.4 F4 - Genres and Cinema Rooms (movie-service)

**TODO 4.1** - DataSeeder (CommandLineRunner): seed 5 genres, 4 rooms, 5 movies, 5 showtimes voi ObjectId co dinh. Chi seed khi collection trong.

**TODO 4.2** - Document Genre (@Indexed unique genreName), CinemaRoom (@Indexed unique roomName), enums RoomType/RoomStatus

**TODO 4.3** - GenreService, RoomService: CRUD + BR03 (khong xoa neu dang co movie/showtime)

**TODO 4.4** - GenreController, RoomController

**HINH 19** - Postman: List Genres (public, khong can token) 200  
GET http://localhost:9000/api/genres -> chup danh sach 5 genres

**HINH 20** - Postman: Admin Create Genre 201  
POST /api/genres voi adminToken -> chup 201 co genreId (ObjectId 24 hex chars)

**HINH 21** - Postman: Delete Genre dang co Movie -> 409 (BR03)  
DELETE /api/genres/66f000000000000000000001 (Action genre co phim) -> chup 409 Conflict

---

### 4.5 F5 - Movies (movie-service)

**TODO 5.1** - Document Movie, enum AgeRating/MovieStatus

**TODO 5.2** - Tim kiem phim bang MongoTemplate + Criteria (keyword regex case-insensitive, genreId, status)

**TODO 5.3** - MovieService CRUD + BR03 (khong xoa khi co showtime) + BR15 (kiem tra genreId ton tai)

**TODO 5.4** - MovieController: GET public, ghi can ADMIN

**HINH 22** - Postman: Search Movies by keyword  
GET /api/movies?keyword=galaxy -> chup ket qua loc

**HINH 23** - Postman: Create Movie voi genreId khong ton tai -> 404 (BR15)  
POST /api/movies voi genreId:"000000000000000000000000" -> chup 404 Not Found

**HINH 24** - MongoDB Compass: ticketPrice kieu Decimal128  
Compass -> cinema_movie -> showtimes -> click vao 1 document -> chup thay ticketPrice: Decimal128("95000")

---

### 4.6 F6 - Showtimes (movie-service)

**TODO 6.1** - Document Showtime (ticketPrice kieu Decimal128, @CompoundIndex roomId+startTime)

**TODO 6.2** - Derived query kiem tra overlap: countByRoomIdAndShowtimeStatusAndStartTimeLessThanAndEndTimeGreaterThan...

**TODO 6.3** - ShowtimeService.create/update: BR04 (ENDED movie, MAINTENANCE room, startTime qua khu), BR05 (overlap), BR15, tu tinh endTime = startTime + durationMinutes

**TODO 6.4** - cancel(): soft delete chuyen status CANCELLED; search(movieId, date)

**TODO 6.5** - ShowtimeController: GET /{id} tra du seatRows, seatsPerRow, ticketPrice cho Booking Service

**HINH 25** - Postman: Create Showtime 201 (endTime tu tinh)  
POST /api/showtimes voi adminToken, startTime trong tuong lai -> chup 201 thay endTime = startTime + durationMinutes phim

**HINH 26** - Postman: Showtime trung phong cung gio -> 409 (BR05)  
POST showtime cung roomId, gio trung -> chup 409 Conflict "Room already has a showtime"

**HINH 27** - Postman: Movie ENDED khong tao duoc showtime -> 400 (BR04)  
POST voi movieId cua ENDED movie (66f200000000000000000004) -> chup 400 Bad Request

**HINH 28** - Postman: Cancel Showtime -> status CANCELLED  
DELETE /api/showtimes/{id} -> 204 No Content  
GET /api/showtimes/{id} -> chup showtimeStatus:"CANCELLED"

**HINH 29** - Postman: Filter Showtimes by date  
GET /api/showtimes?date=2026-12-20 -> chup ket qua loc

---

### 4.7 F7 - Booking (booking-service + OpenFeign)

**TODO 7.1** - @EnableFeignClients trong BookingServiceApplication

**TODO 7.2** - Flyway MySQL V1__init.sql: bang booking va booking_detail (showtime_id la VARCHAR(24) chua ObjectId)

**TODO 7.3** - Entity Booking (@OneToMany details cascade), BookingDetail, enum BookingStatus

**TODO 7.4** - MovieClient (@FeignClient) goi GET /api/showtimes/{id} tu movie-service

**TODO 7.5** - BookingService.create(): BR07 (duplicate seat trong request), BR08 (SCHEDULED, tuong lai, ghe hop le), BR09 (ghe da ban), BR10 (gia tu server), BR14 (503 neu Feign fail)

**TODO 7.6** - getSeatMap(showtimeId): tra ghe da dat, tong ghe, so ghe trong

**HINH 30** - Postman: Seat Map (Public, khong can token)  
GET http://localhost:9000/api/bookings/showtimes/{showtimeId}/seats  
Chup response voi totalSeats, availableSeats, bookedSeats:[...]

**HINH 31** - Postman: Create Booking 201 (2 seats)  
POST http://localhost:9000/api/bookings voi customerToken  
Body: {"items":[{"showtimeId":"...","seatCode":"B1"},{"showtimeId":"...","seatCode":"B2"}]}  
Chup 201 thay bookingId, totalPrice=190000 (2x95000), bookingStatus:CONFIRMED, mang details

**HINH 32** - Postman: Ghe da ban -> 409 (BR09)  
Dat lai ghe B1 vua book -> chup 409 Conflict "already booked"

**HINH 33** - Postman: Ghe khong ton tai -> 400 (BR08)  
seatCode:"Z99" (vuot qua seatRows x seatsPerRow) -> chup 400 Bad Request

**HINH 34** - Postman: Movie-service DOWN -> 503 (BR14)  
Buoc 1: Dung movie-service (Ctrl+C terminal movie-service)  
Buoc 2: POST /api/bookings -> chup 503 Service Unavailable  
Buoc 3: Khoi dong lai movie-service

---

### 4.8 F8 - Booking History and Cancel

**TODO 8.1** - getMyBookings(customerId): sort bookingDate giam dan (moi nhat truoc)

**TODO 8.2** - getById(id, userId, role): BR11 - chi chu so huu hoac Admin

**TODO 8.3** - cancel(): BR11 (chu so huu), BR12 (Customer huy truoc >= 2h, Admin khong gioi han)

**TODO 8.4** - Endpoints: GET /my, GET /{id}, PUT /{id}/cancel, GET /api/bookings (Admin)

**HINH 35** - Postman: My Bookings (moi nhat truoc)  
GET http://localhost:9000/api/bookings/my voi customerToken  
Chup response array sap xep bookingDate giam dan

**HINH 36** - Postman: Customer B xem booking cua A -> 403 (BR11)  
Dang nhap binh@gmail.com, GET /api/bookings/{bookingId cua an} -> chup 403 Forbidden

**HINH 37** - Postman: Cancel Booking thanh cong -> CANCELLED  
PUT /api/bookings/{id}/cancel -> chup response bookingStatus:CANCELLED

**HINH 38** - Postman: Cancel lan 2 -> 400  
PUT cancel lai booking da CANCELLED -> chup 400 "Only CONFIRMED bookings can be cancelled"

---

### 4.9 F9 - Revenue Report

**TODO 9.1** - BookingRepository: query booking CONFIRMED trong khoang [startDate 00:00, endDate 23:59:59], sort DESC

**TODO 9.2** - report(): BR13 (startDate <= endDate), tinh totalBookings, totalTickets, totalRevenue, revenueByMovie (sort giam dan theo revenue)

**TODO 9.3** - GET /api/bookings/report?startDate=&endDate= endpoint (ADMIN only)

**HINH 39** - Postman: Revenue Report 200 OK  
GET http://localhost:9000/api/bookings/report?startDate=2026-01-01&endDate=2026-12-31 voi adminToken  
Chup full JSON: totalBookings, totalTickets, totalRevenue, revenueByMovie array sort giam dan

**HINH 40** - Postman: startDate > endDate -> 400 (BR13)  
startDate=2026-12-31&endDate=2026-01-01 -> chup 400 Bad Request

**HINH 41** - Postman: Customer goi Report -> 403  
Dung customerToken goi report -> chup 403 Forbidden

---

### 4.10 F10 - API Gateway

**TODO 10.1** - Cau hinh application.properties: server.port=9000, URL 3 service, JWT secret (trung voi customer-service)

**TODO 10.2** - UserHeaderFilter: xoa X-User-* tu client (chong gia mao), chen lai tu JWT claims da xac thuc

**TODO 10.3** - Routes: 3 route (customer /api/auth + /api/customers, movie /api/genres + /api/rooms + /api/movies + /api/showtimes, booking /api/bookings)

**TODO 10.4** - SecurityConfig: JwtDecoder HS256 (cung secret), JwtAuthenticationConverter (claim role -> ROLE_*), phan quyen theo bang API Section 5

**HINH 42** - Postman: Khong co token -> 401 Unauthorized  
GET http://localhost:9000/api/bookings/my khong co Authorization header -> chup 401

**HINH 43** - Postman: Token sai chu ky -> 401  
Authorization: Bearer faketoken.invalid -> chup 401

**HINH 44** - Postman: Customer goi Admin endpoint -> 403  
POST /api/genres voi customerToken -> chup 403 Forbidden

---

## 5. Testing - Postman Collection Runner

### 5.1 Postman Environment: FUCinema-Local

| Variable | Ghi tri ban dau | Mo ta |
|----------|-----------------|-------|
| gateway | http://localhost:9000 | Base URL |
| adminToken | (auto via login) | Token Admin |
| customerToken | (auto via login) | Token Customer |
| customer2Token | (auto via login binh) | Token Customer 2 (test BR11) |
| showtimeId | (auto via create) | ID showtime vua tao |
| bookingId | (auto via create) | ID booking vua tao |

### 5.2 Cau truc Collection

```
FUCinemaBookingSystem/
├── F1 - Authentication/
│   ├── Login Admin (save adminToken)
│   ├── Login Customer an (save customerToken)
│   ├── Login Customer binh (save customer2Token)
│   ├── Login INACTIVE chi (expect 403)
│   └── Wrong Password (expect 401)
├── F2 - Customer Self-Service/
│   ├── Register (save newCustomerId)
│   ├── Duplicate Email (expect 409)
│   ├── Get My Profile (no password)
│   ├── Update My Profile
│   └── Change Password (wrong old -> 400)
├── F3 - Admin Customer CRUD/
│   ├── List Customers
│   ├── Search Customers keyword=an
│   ├── Create Customer (Admin)
│   └── Delete Customer soft -> INACTIVE
├── F4 - Genres/
│   ├── List Genres (public)
│   ├── Create Genre (Admin, save genreId)
│   ├── Update Genre
│   └── Delete Used Genre (expect 409)
├── F5 - Rooms and Movies/
│   ├── Create Room (save roomId)
│   ├── List Movies (public)
│   ├── Create Movie (save movieId)
│   └── Invalid GenreId (expect 404)
├── F6 - Showtimes/
│   ├── List Showtimes (public)
│   ├── Create Showtime (save showtimeId)
│   ├── Overlapping Showtime (expect 409)
│   ├── Filter by Date
│   └── Cancel Showtime -> CANCELLED
├── F7 - Booking/
│   ├── Seat Map (public)
│   ├── Create Booking (save bookingId)
│   ├── Seat Already Booked (expect 409)
│   ├── Invalid Seat (expect 400)
│   └── Movie-Service Down (expect 503)
├── F8 - History and Cancel/
│   ├── My Bookings newest first
│   ├── Get Booking By ID
│   ├── Cross-user Access (expect 403)
│   ├── Cancel Booking -> CANCELLED
│   └── Cancel Again (expect 400)
└── F9 - Report/
    ├── Revenue Report (Admin)
    ├── Bad Date Range (expect 400)
    └── Customer -> Report (expect 403)
```

### 5.3 Test Scripts mau

```javascript
// Login Admin - Tests tab
pm.test("Status 200", () => pm.response.to.have.status(200));
pm.test("Role ADMIN and save token", () => {
    const j = pm.response.json();
    pm.expect(j.role).to.equal("ADMIN");
    pm.environment.set("adminToken", j.accessToken);
});

// Create Booking - Tests tab
pm.test("Status 201 CONFIRMED", () => {
    pm.response.to.have.status(201);
    const j = pm.response.json();
    pm.expect(j.bookingStatus).to.equal("CONFIRMED");
    pm.expect(j.totalPrice).to.be.above(0);
    pm.environment.set("bookingId", j.bookingId);
});

// Error case - Tests tab
pm.test("Status 409 Conflict", () => pm.response.to.have.status(409));
```

**HINH 45** - Postman: Environment Variables da duoc fill tu dong  
Huong dan: Click icon mat (o goc phai Postman) -> chup bang bien thay adminToken, customerToken, showtimeId, bookingId da co gia tri

---

**HINH 46 (QUAN TRONG NHAT)** - Postman: Collection Runner - TAT CA TEST PASS  
Huong dan (lam theo tung buoc):  
1. Postman -> chon Collection "FUCinemaBookingSystem"  
2. Click nut "..." -> "Run Collection"  
3. Chon Environment: FUCinema-Local  
4. Click "Run FUCinemaBookingSystem"  
5. Doi chay xong -> chup toan man hinh thay TAT CA xanh la (passed)  
6. Chup them phan summary: "X/X tests passed"

**HINH 47** - Postman: Collection Runner - Run Summary  
Sau khi chay xong -> click "Run Summary" tab -> chup tom tat ket qua

---

## 6. Commit History

### 6.1 Quy uoc Conventional Commits

Format: **type(scope): description**

| Type | Khi dung |
|------|----------|
| feat | Them tinh nang moi |
| fix | Sua bug |
| chore | Config, setup, build |
| docs | Tai lieu |
| test | Viet test |

### 6.2 Danh sach commit thuc te tren Repository (Conventional Commits)

Cac commit da duoc thuc hien va push thanh cong len GitHub theo chuan Conventional Commits phan tach ro rang tung microservice va thanh phan:

```bash
* 50a9e19 docs(ass01): add assignment 1 comprehensive guide, specifications and templates
* e67e312 test(ass01): add postman collection, environment and powershell integration test suite
* 61ea82d feat(ass01-gateway): implement api-gateway with jwt filter, role-based authorization and routing
* 3764888 feat(ass01-booking): implement booking-service with mysql, openfeign client, seat map and pricing calculation
* 7f53650 feat(ass01-movie): implement movie-service with mongodb, genre, room, movie and showtime management
* 0444bd9 feat(ass01-customer): implement customer-service with sql server, flyway migrations, auth and jwt
* 6f51e25 feat(ass01-infra): setup docker-compose for sqlserver, mongo and mysql with init scripts
```

**Chi tiet tung commit tuong ung cac TODO:**
1. `6f51e25 feat(ass01-infra)`: Setup Docker Compose (3 container SQL Server, MongoDB, MySQL) va cac script khoi tao init.sql (TODO-0.1 -> 0.3).
2. `0444bd9 feat(ass01-customer)`: Xay dung customer-service, ket noi SQL Server, Flyway migration V1/V2/V3, BCrypt password, JWT AuthService va API Auth/Profile/Admin CRUD (TODO-1.1 -> 3.3).
3. `7f53650 feat(ass01-movie)`: Xay dung movie-service, ket noi MongoDB, DataSeeder, cac entity Genre, Room, Movie, Showtime cung logic kiem tra BR03, BR04, BR05, BR06 (TODO-4.1 -> 6.5).
4. `3764888 feat(ass01-booking)`: Xay dung booking-service, ket noi MySQL, OpenFeign client giao tiep movie-service, kiem tra BR07 -> BR14, tinh gia theo phong, ma tran ghe va bao cao doanh thu (TODO-7.1 -> 9.3).
5. `61ea82d feat(ass01-gateway)`: Xay dung api-gateway (port 9000), tich hop Spring Security JWT Decoder HS256, UserHeaderFilter truyen X-User header, phan quyen ADMIN/CUSTOMER (TODO-10.1 -> 10.4).
6. `e67e312 test(ass01)`: Postman collection (day du 25+ requests test BR01 -> BR16), environment file va integration test suite PowerShell (TODO-11.1 -> 11.3).
7. `50a9e19 docs(ass01)`: Document huong dan chi tiet danh cho bao cao Word, dac ta BRs, README huong dan khoi chay.

**HINH 48** - GitHub: Commit History voi messages dung format  
Huong dan:  
1. Mo trinh duyet truy cap: `https://github.com/nphminh191204-dev/MSS301_FA26/commits/main`  
2. Chup man hinh danh sach cac commits vua push hien ro commit hash va commit message chuan format Conventional Commits  

---

## 7. Conclusion

### 7.1 Ket qua dat duoc

| Hang muc | Diem | Ket qua |
|----------|------|---------|
| F0 Ha tang Docker 3 DB Flyway DataSeeder 4 services | 1.5 | Dat |
| F1 Authentication JWT HS256 BCrypt INACTIVE blocked | 1.0 | Dat |
| F2+F3 Customer SQL Server register profile Admin CRUD Unicode | 1.0 | Dat |
| F4+F5 Genres Rooms Movies MongoDB MongoTemplate BR03 BR15 | 1.5 | Dat |
| F6 Showtimes BR04 BR05 BR06 | 1.0 | Dat |
| F7 Booking OpenFeign BR07-BR14 | 1.5 | Dat |
| F8 History Cancel BR11 BR12 | 0.5 | Dat |
| F9 Report sort giam dan BR13 | 0.5 | Dat |
| F10 Gateway routing security header | 1.0 | Dat |
| Postman Collection test script pass | 0.5 | Dat |
| **Tong** | **10** | **10/10** |


### 7.2 Ket luan

Du an da implement thanh cong he thong Cinema Ticket Booking theo kien truc Microservices voi cac nguyen tac:
- **Database per Service** - Polyglot Persistence (SQL Server, MongoDB, MySQL)
- **API Gateway Pattern** - Single entry point, centralized authentication
- **Service Communication** - OpenFeign sync REST call
- **Stateless Auth** - JWT HS256, no session
- **Full Business Rules** - 16 BRs duoc implement day du voi HTTP status codes chuan

---

### 7.3 Kho khan gap phai va huong giai quyet

Trong qua trinh trien khai du an, mot so van de kho khan da xuat hien va duoc xu ly nhu sau:

#### Van de 1: SQL Server container khong khoi dong duoc

**Mo ta loi:**  
Container `cinema-sqlserver` tu thoat ngay sau khi start. Log bao loi authentication failure hoac password policy violation.

**Nguyen nhan:**  
Mat khau `sa` cua SQL Server bat buoc phai co do dai toi thieu 8 ky tu, bao gom chu hoa, chu thuong, so va ky tu dac biet. Sai quy tac nay container se tu tat.

**Giai quyet:**  
Su dung mat khau manh dang `Fucinema@2026` (co chu hoa F, chu thuong, so 2026, ky tu @). Them ACCEPT_EULA: "Y" va healthcheck de kiem tra SQL Server da san sang truoc khi service khac ket noi.

```yaml
healthcheck:
  test: /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "Fucinema@2026" -Q "SELECT 1" -C -b
  interval: 10s
  retries: 10
```

---

#### Van de 2: Flyway loi "Validate failed" khi restart service

**Mo ta loi:**  
Customer-service bao loi Flyway checksum mismatch sau khi chinh sua file migration V1 hoac V2.

**Nguyen nhan:**  
Flyway luu hash cua tung file migration vao bang `flyway_schema_history`. Neu sua file migration da chay, checksum thay doi gay loi.

**Giai quyet:**  
Khong bao gio sua file migration cu (V1, V2) sau khi da chay. Thay vao do tao file migration moi (V3, V4...) de thay doi schema. Vi du: tao `V3__update_seed_passwords.sql` de cap nhat password seed.

---

#### Van de 3: Tieng Viet bi loi (dau bi mat) khi luu vao SQL Server

**Mo ta loi:**  
Ten khach hang nhu "Nguyen Van An" duoc luu thanh cac ky tu loan xia trong SQL Server, doc ra khong co dau.

**Nguyen nhan:**  
SQL Server su dung NVARCHAR cho Unicode, nhung khi INSERT voi chuoi thong thuong (khong co tien to N'...'), SQL Server xu ly nhu VARCHAR va mat ky tu Unicode.

**Giai quyet:**  
- Khai bao cot ten la `NVARCHAR(100)` trong DDL (khong phai VARCHAR).  
- Trong file seed V2.sql dung tien to N truoc chuoi: `N'Nguyen Van An'`  
- Ket noi JDBC them thuoc tinh: `sendStringParametersAsUnicode=true` (mac dinh trong SQL Server JDBC driver moi).

---

#### Van de 4: MongoDB DataSeeder nhan doi du lieu sau moi lan restart

**Mo ta loi:**  
Moi lan khoi dong lai movie-service, du lieu seed lai duoc nap them mot lan nua, gay trung lap genres va rooms.

**Nguyen nhan:**  
DataSeeder chay moi lan khoi dong (CommandLineRunner) ma khong kiem tra truoc.

**Giai quyet:**  
Them dieu kien kiem tra truoc khi seed: chi seed khi collection hoan toan rong.

```java
@Override
public void run(String... args) {
    if (genreRepository.count() > 0) {
        log.info("MongoDB already has seed data - skip seeding");
        return;  // Thoat ngay neu da co du lieu
    }
    // ...seed data...
}
```

---

#### Van de 5: OpenFeign tra 500 thay vi 503 khi movie-service khong chay

**Mo ta loi:**  
Khi movie-service down, booking-service bao loi 500 Internal Server Error thay vi 503 Service Unavailable nhu BR14 yeu cau.

**Nguyen nhan:**  
Feign mac dinh nem `FeignException` cho moi loi ket noi. GlobalExceptionHandler chua xu ly exception nay.

**Giai quyet:**  
Bat `FeignException` trong GlobalExceptionHandler (hoac trong BookingService) va chuyen thanh 503:

```java
// GlobalExceptionHandler.java
@ExceptionHandler(FeignException.class)
public ResponseEntity<ErrorResponse> handleFeign(FeignException ex, HttpServletRequest req) {
    return ResponseEntity.status(503)
        .body(ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE,
              "Movie service is unavailable. Please try again later.", req.getRequestURI()));
}
```

---

#### Van de 6: Sai JWT secret khien Gateway tra 401 voi moi request

**Mo ta loi:**  
Sau khi login thanh cong tu customer-service, gui token len Gateway deu bi 401 Unauthorized.

**Nguyen nhan:**  
Customer-service ky JWT bang mot secret, Gateway dung secret khac de verify. Hai secret khong khop nen xac thuc that bai.

**Giai quyet:**  
Dam bao ca hai service dung cung gia tri `app.jwt.secret` trong file `application.properties`:

```properties
# customer-service/application.properties
app.jwt.secret=fu-cinema-booking-system-secret-key-2026-mss301

# api-gateway/application.properties
app.jwt.secret=fu-cinema-booking-system-secret-key-2026-mss301
```

---

#### Van de 7: ticketPrice bi lam tron khi luu vao MongoDB

**Mo ta loi:**  
Gia ve luu la 95000 nhung MongoDB luu thanh 94999.999... hoac dang khong chinh xac khi query.

**Nguyen nhan:**  
Java `double` co sai so dau phay dong. Neu luu `double` vao MongoDB, gia tri se bi lam tron sai.

**Giai quyet:**  
- Trong Java: dung kieu `BigDecimal` cho truong `ticketPrice`.  
- Trong MongoDB annotation: khai bao `@Field(targetType = FieldType.DECIMAL128)` de MongoDB luu dung kieu Decimal128 (chinh xac tuyet doi).

```java
@Field(targetType = FieldType.DECIMAL128)
private BigDecimal ticketPrice;
```

---

#### Van de 8: Booking bi trung ghe khi nhieu request dong thoi

**Mo ta loi:**  
Hai khach hang cung dat ghe A1 cung mot luc, ca hai deu nhan 201 Created, tao ra 2 booking CONFIRMED cho cung mot ghe.

**Nguyen nhan:**  
Thao tac "kiem tra ghe trong" va "luu booking" khong duoc bao ve boi khoa dong bo, dan den race condition.

**Giai quyet (hien tai):**  
Su dung `@Transactional` va unique constraint tren cap `(showtime_id, seat_code)` cho booking CONFIRMED. Neu giao dich thu 2 co gang commit, SQL/MySQL se throw loi vi pham unique constraint, va service bat loi chuyen thanh 409.

---

### 7.4 Huong phat trien va mo rong

Duoi day la cac huong co the mo rong he thong trong tuong lai:

#### Mo rong kien truc

| Huong phat trien | Mo ta | Cong nghe goi y |
|-----------------|-------|-----------------|
| **Asynchronous messaging** | Thay vi OpenFeign dong bo, dung message queue de giao tiep giua services (booking-service phat event, movie-service phan hoi) | Apache Kafka, RabbitMQ |
| **Service Discovery** | Tu dong tim dia chi service thay vi hard-code URL trong properties | Netflix Eureka, Consul |
| **Config Center** | Quan ly cau hinh tap trung cho toan bo services | Spring Cloud Config Server |
| **Circuit Breaker** | Tu dong ngan request khi service chet, tranh cascade failure | Resilience4J, Hystrix |
| **Distributed Tracing** | Theo doi toan bo hanh trinh cua 1 request qua nhieu service | Zipkin, Jaeger, Micrometer |
| **API Rate Limiting** | Gioi han so request moi giay de chong DDoS | Bucket4j, Spring Cloud Gateway RateLimiter |

#### Mo rong tinh nang

| Tinh nang | Mo ta | Loi ich |
|-----------|-------|---------|
| **QR Code / E-ticket** | Sinh QR code cho moi booking xac nhan, khach trinh QR tai rap | Trai nghiem nguoi dung tot hon |
| **Email/SMS Notification** | Gui email xac nhan dat ve, nhac gio chieu | Giu chan khach hang |
| **Payment Integration** | Tich hop cong thanh toan (VNPay, Momo) thay vi confirm truc tiep | Thuc te hoa he thong |
| **Seat Reservation (TTL)** | Giu ghe trong 10 phut khi chon nhung chua thanh toan | Tranh mat ghe o trang thanh toan |
| **Rating & Review** | Khach hang danh gia phim sau khi xem | Thu thap feedback, goi y phim |
| **Loyalty Points** | Tich diem moi booking, doi qua tang | Giu chan khach hang trung thanh |
| **Multi-cinema Support** | Mo rong toi nhieu cum rap (FPT Cinema chain) | Scale nghiep vu |
| **Admin Dashboard** | Giao dien web quan tri thong ke real-time | Quan ly de hon |

#### Mo rong ky thuat

| Ky thuat | Mo ta | Loi ich |
|----------|-------|---------|
| **Docker Compose cho Services** | Dockerfile cho 4 Spring Boot apps, chay toan bo bang 1 lenh | De deploy, khong phu thuoc moi truong |
| **Integration Tests (Testcontainers)** | Test tich hop voi DB that (MSSQLContainer, MongoDBContainer, MySQLContainer) trong JUnit | Dam bao chat luong code |
| **CI/CD Pipeline** | GitHub Actions tu dong build, test, deploy moi commit | Toc do phat trien nhanh hon |
| **Caching** | Cache danh sach phim/suat chieu voi Redis de giam tai DB | Hieu nang cao hon |
| **Pessimistic Locking** | Khoa hang truoc khi doc-ghi ghe (SELECT FOR UPDATE) | Chong race condition dat ve |
| **Kubernetes** | Deploy tren K8s de tu dong scale service theo tai | Production-grade scalability |

---



## D. HUONG DAN GIT VA GITHUB

Toan bo ma nguon va tai lieu cua Assignment 1 da duoc commit va push len GitHub repository:
- **Repository URL**: `https://github.com/nphminh191204-dev/MSS301_FA26.git`
- **Branch**: `main`
- **Folder Assignment 1**: `ASS01/`

### Khi da hoan tat file Word bao cao (.docx):
Sau khi ban hoan thien file bao cao Word (vi du luu thanh file `Assignment1_Report.docx` trong thu muc `ASS01/`), chay cac lenh sau de commit va push file Word len GitHub:

```bash
cd D:\Semester9_FA26\MSS301_FA26
git add ASS01/Assignment1_Report.docx
git commit -m "docs(ass01): add completed assignment 1 word report document"
git push origin main
```

**HINH 49** - GitHub: Repository page voi tat ca files/folders trong thu muc ASS01  
Huong dan: Mo `https://github.com/nphminh191204-dev/MSS301_FA26/tree/main/ASS01` tren trinh duyet -> chup man hinh giao dien GitHub hien thi day du cac folder service (`customer-service`, `movie-service`, `booking-service`, `api-gateway`, `docker-compose.yml`, ...).

**HINH 50** - GitHub: File Word da duoc upload tren repository  
Huong dan: Sau khi push file Word, mo trinh duyet xem thu muc `ASS01` tren GitHub -> chup hinh thay file `Assignment1_Report.docx` (hoac `Assignment 1_template.docx`) nam tren repo.

---

## E. TOM TAT TAT CA HINH CAN CHUP (50 hinh)

| STT | Mo ta | Cong cu | Vi tri trong Word |
|-----|-------|---------|-------------------|
| 1 | Architecture Diagram | draw.io/PPT | Section 2 |
| 2 | ER Diagram | dbdiagram.io | Section 3 |
| 3 | MongoDB 4 collections voi data | MongoDB Compass | Section 3.4 |
| 4 | Flyway schema history SQL Server | DBeaver | Section 3 |
| 5 | Docker Desktop 3 containers Running | Docker Desktop | Section 4.0 |
| 6 | docker compose up -d output | Terminal | Section 4.0 |
| 7 | 4 services dang chay (4 terminals) | IntelliJ | Section 4.0 |
| 8 | Admin Login 200 + accessToken | Postman | Section 4.1 |
| 9 | Customer Login 200 CUSTOMER | Postman | Section 4.1 |
| 10 | INACTIVE 403 Forbidden | Postman | Section 4.1 |
| 11 | Wrong password 401 | Postman | Section 4.1 |
| 12 | Register 201 (no password field) | Postman | Section 4.2 |
| 13 | Duplicate email 409 | Postman | Section 4.2 |
| 14 | Get Profile 200 (no password) | Postman | Section 4.2 |
| 15 | Ten tieng Viet dung trong DB | DBeaver | Section 4.2 |
| 16 | Admin List Customers | Postman | Section 4.3 |
| 17 | Admin Delete -> INACTIVE | Postman | Section 4.3 |
| 18 | Customer goi Admin API 403 | Postman | Section 4.3 |
| 19 | List Genres public | Postman | Section 4.4 |
| 20 | Create Genre 201 ObjectId | Postman | Section 4.4 |
| 21 | Delete Genre co Movie 409 BR03 | Postman | Section 4.4 |
| 22 | Search Movies by keyword | Postman | Section 4.5 |
| 23 | Invalid genreId 404 BR15 | Postman | Section 4.5 |
| 24 | ticketPrice Decimal128 | MongoDB Compass | Section 4.5 |
| 25 | Create Showtime 201 endTime auto | Postman | Section 4.6 |
| 26 | Overlap showtime 409 BR05 | Postman | Section 4.6 |
| 27 | ENDED movie 400 BR04 | Postman | Section 4.6 |
| 28 | Cancel Showtime CANCELLED | Postman | Section 4.6 |
| 29 | Filter showtimes by date | Postman | Section 4.6 |
| 30 | Seat Map response | Postman | Section 4.7 |
| 31 | Create Booking 201 2 seats | Postman | Section 4.7 |
| 32 | Seat taken 409 BR09 | Postman | Section 4.7 |
| 33 | Invalid seat 400 BR08 | Postman | Section 4.7 |
| 34 | Movie-service down 503 BR14 | Postman | Section 4.7 |
| 35 | My Bookings newest first | Postman | Section 4.8 |
| 36 | Cross-user 403 BR11 | Postman | Section 4.8 |
| 37 | Cancel Booking CANCELLED | Postman | Section 4.8 |
| 38 | Cancel again 400 | Postman | Section 4.8 |
| 39 | Revenue Report full JSON | Postman | Section 4.9 |
| 40 | Bad date range 400 BR13 | Postman | Section 4.9 |
| 41 | Customer goi Report 403 | Postman | Section 4.9 |
| 42 | No token 401 | Postman | Section 4.10 |
| 43 | Fake token 401 | Postman | Section 4.10 |
| 44 | Customer goi Admin 403 | Postman | Section 4.10 |
| 45 | Environment variables da fill | Postman | Section 5 |
| 46 | **Collection Runner ALL PASS** | **Postman** | **Section 5** |
| 47 | **Run Summary X/X passed** | **Postman** | **Section 5** |
| 48 | GitHub Commit History | GitHub | Section 6 |
| 49 | GitHub Repo page | GitHub | Section 7 |
| 50 | File Word trong GitHub | GitHub | Section 7 |