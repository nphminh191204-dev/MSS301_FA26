# Practical Exam for 2026 Summer – Page #1

## 1. General Information

| Field | Detail |
| :--- | :--- |
| **Subject Code** | MSS301 |
| **Subject Name** | Microservices with Spring Cloud |
| **Time** | 90 minutes (include submit time) |
| **Exam Type** | Practical Exam |
| **Note** | a) Allow student to connect to Internet within 10 minutes when setting up project at the beginning of examination.<br>b) Student can only use resources in local computer. |

---

## 2. Exam Requirements

Develop the following Microservices projects in Spring Boot, Spring Cloud, and related libraries:
- **Room Service**
- **Reservation Service**
- **Hotel Gateway**

---

### 2.1 Room Service

| Attribute | Value |
| :--- | :--- |
| **Project Name** | `<StudentID>RoomService` |
| **Framework** | Spring Boot >= 3.5.11, Spring MVC, Spring JPA, Swagger |
| **Database Server** | MS SQL Server 2016 or above |
| **Architecture** | Layered Architecture (Controller -> Service -> Repository) |
| **Requirements** | Develop the following operations for the services according to the service specification on Spring Boot and other frameworks |

#### 2.1.1 Configuration for `application.properties`
The following configuration must be followed to run the RESTful API application:

| # | Items | Value |
| :- | :--- | :--- |
| 1 | `spring.datasource.password` | `sa` |
| 2 | `spring.datasource.username` | `sa` |
| 3 | `spring.datasource.url` | `jdbc:sqlserver://localhost:1433;databaseName=MSS301_2026_PE;encrypt=false;` |
| 4 | `spring.jpa.hibernate.ddl-auto` | `none` |
| 5 | `server.port` | `8081` |
| 6 | Context path | default |
| 7 | Database name | `MSS301_2026_PE` |

---

#### 2.1.2 Service Specification

##### 2.1.2.1 Data Transfer Objects (DTOs)

**ApiResponseDTO**
| # | Attribute name | Type | Description |
| :- | :--- | :--- | :--- |
| 1 | `status` | `int` | |
| 2 | `data` | `Object` | Contains data object: can be null, a single PageDTO, or a list of objects |
| 3 | `timestamp` | `String` | Format: `YYYY-MM-DDThh:mm:ssZ` |
| 4 | `message` | `String` | Contain error message |

**RoomDTO**
| # | Attribute name | Type | Description |
| :- | :--- | :--- | :--- |
| 1 | `roomId` | `long` | |
| 2 | `roomNumber` | `String` | Unique room identifier |
| 3 | `roomType` | `Enum` | (`SINGLE`, `DOUBLE`, `SUITE`, `DELUXE`) |
| 4 | `pricePerNight` | `BigDecimal` | Must be > 0 |
| 5 | `capacity` | `int` | Maximum number of guests (1–10) |
| 6 | `floor` | `int` | Floor number (>= 1) |
| 7 | `status` | `Enum` | (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`) |

**PageDTO**
| # | Attribute name | Type | Description |
| :- | :--- | :--- | :--- |
| 1 | `size` | `int` | |
| 2 | `page` | `int` | |
| 3 | `totalPages` | `int` | |
| 4 | `totalElements` | `long` | |
| 5 | `first` | `boolean` | True if the current page is the first page |
| 6 | `last` | `boolean` | True if the current page is the last page |
| 7 | `content` | `Object` | It is a `List<RoomDTO>` |

**Data Structure samples:**
- **ApiResponseDTO:**
  ```json
  {
    "status": 1,
    "message": "Successful",
    "data": { ... },
    "timestamp": "2026-07-19T02:21:45Z"
  }
  ```
- **RoomDTO:**
  ```json
  {
    "roomId": 1,
    "roomNumber": "A-101",
    "roomType": "DOUBLE",
    "pricePerNight": 850000,
    "capacity": 2,
    "floor": 1,
    "status": "AVAILABLE"
  }
  ```
- **PageDTO:**
  ```json
  {
    "size": 10,
    "page": 0,
    "totalPages": 4,
    "totalElements": 40,
    "first": true,
    "last": false,
    "content": [ { ... } ]
  }
  ```

---

##### 2.1.2.2 API Endpoints
**Controller Class:** `<StudentID>RoomController`

###### 1. Create Room
- **Method & Path:** `POST /api/rooms`
- **Description:** Create a new room in the system.
- **Request Body:** `RoomDTO` (Creates a new room with the provided details. All required fields must be present and valid.)
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **201** | 1 | Room created successfully. Returns the created `RoomDTO` in `ApiResponseDTO`. Default status is `AVAILABLE`. |
| **406** | 2 | Data validation failed (missing required fields, invalid values...). No data returned. |
| **226** | 3 | Room number is duplicated. No data returned. |
| **500** | 0 | Internal server error. Failed to create room. No data returned. |

---

###### 2. Update Room
- **Method & Path:** `PUT /api/rooms/{roomId}`
- **Description:** Update a room in the system by room Id.
- **Path Parameter:** `roomId` (`long`) — The unique identifier of the room to update.
- **Request Body:** `RoomDTO` (Partially updates a room. Only provided fields are updated; omitted fields retain their current values.)
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Room updated successfully. Returns the updated `RoomDTO` in `ApiResponseDTO`. |
| **406** | 2 | Data validation failed. No data returned. |
| **226** | 3 | Room number is duplicated. No data returned. |
| **404** | 4 | Room is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to update room. No data returned. |

---

###### 3. Get Room Detail
- **Method & Path:** `GET /api/rooms/{roomId}`
- **Description:** Get details of a room in the system by room Id.
- **Path Parameter:** `roomId` (`long`) — The unique identifier of the room to retrieve.
- **Request Body:** None
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Returns the `RoomDTO` object in `ApiResponseDTO`. |
| **404** | 4 | Room is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to get room. No data returned. |

---

###### 4. Set Room to Maintenance
- **Method & Path:** `DELETE /api/rooms/{roomId}`
- **Description:** Set a room to maintenance status by room Id.
- **Path Parameter:** `roomId` (`long`) — The unique identifier of the room. Sets the Room status to `MAINTENANCE`. A room in `MAINTENANCE` cannot be reserved.
- **Request Body:** None
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Room set to MAINTENANCE successfully. No data returned. |
| **404** | 4 | Room is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to update room status. No data returned. |

---

###### 5. Get Room List
- **Method & Path:** `GET /api/rooms`
- **Description:** Get a paginated list of rooms in the system.
- **Query Parameters:**
  | Parameter | Type | Required | Description |
  | :--- | :--- | :--- | :--- |
  | `page` | `Integer` | No | Page number (default: 0) |
  | `size` | `Integer` | No | Page size (default: 10, max: 100) |
  | `roomType` | `Enum` | No | Filter by type: `SINGLE`, `DOUBLE`, `SUITE`, `DELUXE` |
  | `status` | `Enum` | No | Filter by status: `AVAILABLE`, `OCCUPIED`, `MAINTENANCE` |
- **Response Behavior:** `ApiResponseDTO`, `PageDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Returns the list of `RoomDTO` objects in `ApiResponseDTO`. |
| **406** | 2 | Data validation failed. No data returned. |
| **500** | 0 | Internal server error. Failed to get rooms. No data returned. |

---

#### 2.1.3 Database Structure
**Table Name:** `rooms`

| # | Field Name | Type | Max Len | Mandatory | Entity Attr Type | Description |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | `room_id` | `Identity` | NA | Yes | `long` | Auto increment |
| 2 | `room_number` | `Nvarchar` | 20 | Yes | `String` | Unique room number |
| 3 | `room_type` | `Nvarchar` | 10 | Yes | `Enum` | (`SINGLE`, `DOUBLE`, `SUITE`, `DELUXE`) |
| 4 | `price_per_night` | `Decimal(18,2)`| | Yes | `BigDecimal` | Must be > 0 |
| 5 | `capacity` | `int` | NA | Yes | `int` | Between 1 and 10 inclusive |
| 6 | `floor` | `int` | | Yes | `int` | Must be >= 1 |
| 7 | `status` | `Nvarchar` | 15 | Yes | `Enum` | (`AVAILABLE`, `OCCUPIED`, `MAINTENANCE`) |

---

### 2.2 Reservation Service

| Attribute | Value |
| :--- | :--- |
| **Project Name** | `<StudentID>ReservationService` |
| **Framework** | Spring Boot >= 3.5.11, Spring MVC, Spring JPA, OpenFeign, Swagger |
| **Database Server** | MS SQL Server 2016 or above |
| **Architecture** | Layered Architecture (Controller -> Service -> Repository) |
| **Requirements** | Develop the following operations for the services according to the service specification on Spring Boot and other frameworks |

#### 2.2.1 Configuration for `application.properties`

| # | Items | Value |
| :- | :--- | :--- |
| 1 | `spring.datasource.password` | `sa` |
| 2 | `spring.datasource.username` | `sa` |
| 3 | `spring.datasource.url` | `jdbc:sqlserver://localhost:1433;databaseName=MSS301_2026_PE;encrypt=false;` |
| 4 | `spring.jpa.hibernate.ddl-auto` | `none` |
| 5 | `server.port` | `8082` |
| 6 | Context path | default |
| 7 | Database name | `MSS301_2026_PE` |

---

#### 2.2.2 Service Specification

##### 2.2.2.1 Data Transfer Objects (DTOs)
- **ApiResponseDTO:** Same structure with `ApiResponseDTO` in Room Service project.

**ReservationDTO**
| # | Attribute name | Type | Description |
| :- | :--- | :--- | :--- |
| 1 | `reservationId` | `long` | |
| 2 | `guestName` | `String` | |
| 3 | `guestEmail` | `String` | |
| 4 | `guestPhone` | `String` | |
| 5 | `checkInDate` | `java.util.Date` | Not include time. **The accepted format `dd/MM/yyyy`** |
| 6 | `checkOutDate` | `java.util.Date` | Must be after checkInDate. **The accepted format `dd/MM/yyyy`** |
| 7 | `numberOfGuests` | `int` | Must not exceed the room capacity |
| 8 | `totalAmount` | `BigDecimal` | Computed = $\text{pricePerNight} \times \text{number of nights}$ |
| 9 | `status` | `Enum` | (`CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`) |
| 10 | `roomId` | `long` | **Check available by RoomService** |

**ReservationDetailDTO**
| # | Attribute name | Type | Description |
| :- | :--- | :--- | :--- |
| 1 | `reservationId` | `long` | |
| 2 | `guestName` | `String` | |
| 3 | `guestEmail` | `String` | |
| 4 | `guestPhone` | `String` | |
| 5 | `checkInDate` | `java.util.Date` | Not include time. **The accepted format `dd/MM/yyyy`** |
| 6 | `checkOutDate` | `java.util.Date` | Must be after checkInDate. **The accepted format `dd/MM/yyyy`** |
| 7 | `numberOfGuests` | `int` | Must not exceed the room capacity |
| 8 | `totalAmount` | `BigDecimal` | Computed = $\text{pricePerNight} \times \text{number of nights}$ |
| 9 | `status` | `Enum` | (`CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED`) |
| 10 | `room` | `RoomDTO` | **Get from RoomService** |

**PageDTO**
- See the definition in Room Service section.

---

##### 2.2.2.2 API Endpoints
**Controller Class:** `<StudentID>ReservationController`

###### 1. Create Reservation
- **Method & Path:** `POST /api/reservations`
- **Description:** Create a new reservation in the system.
- **Request Body:** `ReservationDTO` (Creates a reservation for a room. The room must exist and have status `AVAILABLE`.)
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **201** | 1 | Reservation created successfully. Returns the created `ReservationDTO` in `ApiResponseDTO`. Default status is `CONFIRMED`. |
| **406** | 2 | Data validation failed (missing required fields, invalid values...). No data returned. |
| **404** | 4 | Room ID is not found. No data returned. |
| **400** | 5 | Room is not AVAILABLE for reservation. No data returned. |
| **500** | 0 | Internal server error. Failed to create reservation. No data returned. |

---

###### 2. Update Reservation
- **Method & Path:** `PUT /api/reservations/{reservationId}`
- **Description:** Update a reservation by reservation Id.
- **Path Parameter:** `reservationId` (`long`) — The unique identifier of the reservation to update.
- **Request Body:** `ReservationDTO` (Partially updates a reservation (e.g., dates, status). Only provided fields are updated; omitted fields retain their current values.)
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Reservation updated successfully. Returns the updated `ReservationDTO` in `ApiResponseDTO`. |
| **406** | 2 | Data validation failed. No data returned. |
| **404** | 4 | Reservation ID is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to update reservation. No data returned. |

---

###### 3. Get Reservation Detail
- **Method & Path:** `GET /api/reservations/{reservationId}`
- **Description:** Get details of a reservation by reservation Id.
- **Path Parameter:** `reservationId` (`long`) — The unique identifier of the reservation.
- **Request Body:** None
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Returns the `ReservationDetailDTO` object in `ApiResponseDTO`. |
| **404** | 4 | Reservation is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to get reservation. No data returned. |

---

###### 4. Cancel Reservation
- **Method & Path:** `DELETE /api/reservations/{reservationId}`
- **Description:** Cancel a reservation by reservation Id.
- **Path Parameter:** `reservationId` (`long`) — The unique identifier of the reservation to cancel. Sets the Reservation status to `CANCELLED`.
- **Request Body:** None
- **Response Behavior:** `ApiResponseDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Reservation cancelled successfully. No data returned. |
| **404** | 4 | Reservation is not found. No data returned. |
| **500** | 0 | Internal server error. Failed to cancel reservation. No data returned. |

---

###### 5. Get Reservation List
- **Method & Path:** `GET /api/reservations`
- **Description:** Get a paginated list of reservations in the system.
- **Query Parameters:**
  | Parameter | Type | Required | Description |
  | :--- | :--- | :--- | :--- |
  | `page` | `Integer` | No | Page number (default: 0) |
  | `size` | `Integer` | No | Page size (default: 10, max: 100) |
  | `guestName` | `String` | No | Filter by guest name (partial match) |
  | `status` | `Enum` | No | Filter by status: `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED` |
- **Response Behavior:** `ApiResponseDTO`, `PageDTO`

| HTTP Code | Status | Description |
| :--- | :--- | :--- |
| **200** | 1 | Returns the list of `ReservationDetailDTO` objects in `ApiResponseDTO`. |
| **406** | 2 | Data validation failed. No data returned. |
| **500** | 0 | Internal server error. Failed to get reservations. No data returned. |

---

#### 2.2.3 Database Structure
**Table Name:** `reservations`

| # | Field Name | Type | Max Len | Mandatory | Entity Attr Type | Description |
| :- | :--- | :--- | :--- | :--- | :--- | :--- |
| 1 | `reservation_id` | `Identity` | | Yes | `long` | Auto generated |
| 2 | `guest_name` | `Nvarchar` | 100 | Yes | `String` | |
| 3 | `guest_email` | `Nvarchar` | 100 | Yes | `String` | |
| 4 | `guest_phone` | `Nvarchar` | 20 | Yes | `String` | |
| 5 | `check_in_date` | `Date` | NA | Yes | `java.util.Date` | |
| 6 | `check_out_date`| `Date` | NA | Yes | `java.util.Date` | Must be after check_in_date |
| 7 | `number_of_guests`| `int` | NA | Yes | `int` | Must be >= 1 and <= 10 |
| 8 | `total_amount` | `Decimal(18,2)` | | Yes | `BigDecimal` | Must be >= 0 |
| 9 | `status` | `Nvarchar` | 15 | Yes | `Enum` | `CONFIRMED`, `CHECKED_IN`, `CHECKED_OUT`, `CANCELLED` |
| 10 | `room_id` | `int` | | Yes | `long` | Foreign key |

---

### 2.3 Hotel Gateway

| Attribute | Value |
| :--- | :--- |
| **Project Name** | `<StudentID>HotelGateway` |
| **Framework** | Spring Boot >= 3.5.11, Spring Cloud Gateway, Spring Security, Swagger |
| **Database Server** | N/A |
| **Requirements** | 1. Develop the gateway that must provide a single entry point for client applications connecting to microservices (RoomService, ReservationService).<br>2. Configure CORS to make sure the gateway accepts connections from all hosts. |

#### 2.3.1 Configuration for `application.properties`

| # | Items | Value |
| :- | :--- | :--- |
| 1 | `server.port` | `8080` |
| 2 | Context path is default | |

---

### 2.4 Common Convention & Rules

| Code | Description |
| :--- | :--- |
| **R01** | The input data of the service operation must be validated based on the constraints (mandatory, max length, format…) defined for columns and tables in the database. |
| **R02** | All API requests and responses use JSON format (`Content-Type: application/json`). |
| **R03** | Pagination uses page (0-indexed) and size query parameters. |
| **R04** | Timestamps follow ISO 8601 format (`YYYY-MM-DDThh:mm:ssZ`, e.g., `2026-07-01T14:00:00Z`). |

---

### 2.5 Project Structure Requirements

| # | Description |
| :- | :--- |
| 1 | The project must be Spring Boot with `"jar"` package type, run on JDK 21, in IntelliJ format. The project name must follow the convention in the project description. |
| 2 | Use MS SQL Server Driver enclosed with the Spring Boot Project in IntelliJ for MS SQL Server 2016 or later. |
| 3 | The application must follow the MVC & Layer structural pattern. Package structure:<br>• `fu.<studentId>.<servicename>.entity` — Contain entity classes<br>• `fu.<studentId>.<servicename>.repository` — Contain Repository Interface classes<br>• `fu.<studentId>.<servicename>.service` — Contain service interface classes<br>• `fu.<studentId>.<servicename>.service.impl` — Contain service implementation classes<br>• `fu.<studentId>.<servicename>.controller` — Contain controller classes<br>• `fu.<studentId>.<servicename>.dto` — Contain data transfer objects<br>• `fu.<studentId>.<servicename>.config` — Contain configuration classes<br>• `fu.<studentId>.<servicename>.common` — Contain common classes<br><br>*Note: Replace `<studentId>` with your ID in lower case; replace `<servicename>` with `room`, `reservation`, or `gateway`.* |
| 4 | Class name and class attributes must follow Java coding convention and be named according to requirements. |
| 5 | Please zip the project folder and submit the zip file to EOS. Follow the submit guideline in the enclosed document when submitting your project. |
| 6 | Table name & Field name in the database must follow the defined tables above. If not followed, the result will not be graded. |
| 7 | All services and gateway projects must be configured to enable Swagger and Spring Doc API. |

---

## 3. Grading Policies

> **The following items are mandatory requirements. If one of the items is violated, your result will be graded zero.**  
> *(Các mục sau là các yêu cầu bắt buộc phải tuân thủ. Nếu bài làm vi phạm một trong các mục sau, bài làm sẽ bị chấm điểm 0)*

| # | Item |
| :- | :--- |
| 1 | The requirements noted in the following sections:<br>- **2.5 Project structure requirements**<br>- **Configuration for `application.properties` of the projects** |

---

## 4. PE Submit Guideline

Note that the project sources must be zipped before submitting to PEA. All project sources must be zipped into a single zip file. Please follow the following steps to **zip project sources**:

- **Step 1:** Go to the folder containing the project
- **Step 2:** Right click on **the project folder name contains all project sources** and then select **“send to” $\rightarrow$ “Compressed (zipped) folder”**