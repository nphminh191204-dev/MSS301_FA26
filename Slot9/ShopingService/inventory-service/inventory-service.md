# Hướng dẫn Step-by-Step: Xây dựng Inventory Service

> Dựa trên `part1.md` (nguồn: programmingtechie.com). Package gốc `com.programmingtechie` đã được đổi thành **`com.fudn`**.
> **Database:** MySQL | **Port:** 8082 | **Migration:** Flyway | **Artifact:** `inventory-service`

---

## 0. Checklist tổng quan

- [x] Tạo project Spring Boot tại start.spring.io
- [x] Cập nhật `init.sql` để thêm database `inventory_service` (dùng chung MySQL container với order-service)
- [x] Cấu hình `application.properties`
- [x] Viết Flyway migration `V1__init.sql` và `V2__add_inventory.sql`
- [x] Tạo Model `Inventory`
- [x] Tạo Repository `InventoryRepository`
- [x] Tạo Service `InventoryService`
- [x] Tạo Controller `InventoryController`
- [x] Test thủ công bằng Postman
- [x] Viết Integration Test (TestContainers + RestAssured)
- [x] Chạy `mvn test` thành công

---

## Bước 1 – Tạo project tại start.spring.io

Cấu hình:
- **Group:** `com.fudn` ⚠️ (đã đổi từ `com.programmingtechie`)
- **Artifact:** `inventory-service`
- **Java:** 21, **Maven**

Dependencies (giống Order Service):
- `Spring Web`
- `Lombok`
- `Spring Data JPA`
- `MySQL Driver`
- `Flyway Migration`
- `Testcontainers`

### TODO
- [x] Generate project với Group = `com.fudn`, Artifact = `inventory-service`
- [x] Mở project trong IDE
- [x] `mvn clean verify` chạy không lỗi

---

## Bước 2 – Cập nhật `init.sql` để thêm database `inventory_service`

Mở `mysql/init.sql` (file dùng chung cho MySQL container của order-service), cập nhật thành:

```sql
CREATE DATABASE IF NOT EXISTS order_service;
CREATE DATABASE IF NOT EXISTS inventory_service;
```

> Vì dùng **cùng 1 MySQL container** cho order-service và inventory-service, nên cần tạo cả 2 database trong `init.sql`.

⚠️ Nếu container `mysql` đã chạy trước đó với volume dữ liệu cũ, `init.sql` sẽ **không** chạy lại tự động. Cần xóa volume cũ (`docker/mysql/data`) rồi `docker compose up -d mysql` lại, hoặc tạo database thủ công bằng lệnh SQL trực tiếp.

### TODO
- [x] Cập nhật `mysql/init.sql` với 2 dòng `CREATE DATABASE`
- [x] Nếu cần: reset volume MySQL và chạy lại container
- [x] Xác nhận cả 2 database `order_service` và `inventory_service` tồn tại

---

## Bước 3 – Cấu hình `application.properties`

```properties
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_service
spring.datasource.username=root
spring.datasource.password=mysql
spring.jpa.hibernate.ddl-auto=none
server.port=8082
```

### TODO
- [x] Thêm cấu hình datasource trỏ tới `inventory_service`
- [x] `spring.jpa.hibernate.ddl-auto=none`
- [x] `server.port=8082`

---

## Bước 4 – Tạo Flyway migration scripts

`src/main/resources/db/migration/V1__init.sql`:

```sql
CREATE TABLE `t_inventory`
(
    `id`       bigint(20)   NOT NULL AUTO_INCREMENT,
    `sku_code` varchar(255) DEFAULT NULL,
    `quantity` int(11)      DEFAULT NULL,
    PRIMARY KEY (`id`)
);
```

`src/main/resources/db/migration/V2__add_inventory.sql`:

```sql
INSERT INTO t_inventory (quantity, sku_code)
VALUES (100, 'iphone_15'),
       (100, 'pixel_8'),
       (100, 'galaxy_24'),
       (100, 'oneplus_12');
```

> Khi khởi động, Flyway tự chạy V1 rồi V2 theo thứ tự. Log kỳ vọng:
> `Successfully applied 2 migrations to schema inventory_service, now at version v2`

### TODO
- [x] Tạo `V1__init.sql` (bảng `t_inventory`)
- [x] Tạo `V2__add_inventory.sql` (dữ liệu mẫu 4 SKU)
- [x] Khởi động app, xác nhận log Flyway apply 2 migration thành công

---

## Bước 5 – Tạo Model `Inventory.java`

`src/main/java/com/fudn/inventoryservice/model/Inventory.java`:

```java
package com.fudn.inventoryservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "t_inventory")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String skuCode;
    private Integer quantity;
}
```

### TODO
- [x] Tạo package `com.fudn.inventoryservice.model`
- [x] Tạo entity `Inventory` ánh xạ bảng `t_inventory`

---

## Bước 6 – Tạo Repository `InventoryRepository.java`

`src/main/java/com/fudn/inventoryservice/repository/InventoryRepository.java`:

```java
package com.fudn.inventoryservice.repository;

import com.fudn.inventoryservice.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Spring Data JPA tự sinh query từ tên method:
    // SELECT COUNT(*) > 0 FROM t_inventory WHERE sku_code = ? AND quantity >= ?
    boolean existsBySkuCodeAndQuantityIsGreaterThanEqual(String skuCode, int quantity);
}
```

### TODO
- [x] Tạo interface `InventoryRepository`
- [x] Method `existsBySkuCodeAndQuantityIsGreaterThanEqual` đặt đúng tên theo convention Spring Data

---

## Bước 7 – Tạo Service `InventoryService.java`

`src/main/java/com/fudn/inventoryservice/service/InventoryService.java`:

```java
package com.fudn.inventoryservice.service;

import com.fudn.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode, Integer quantity) {
        return inventoryRepository.existsBySkuCodeAndQuantityIsGreaterThanEqual(skuCode, quantity);
    }
}
```

### TODO
- [x] Tạo class `InventoryService` (`isInStock`)
- [x] Đánh dấu `@Transactional(readOnly = true)` cho method chỉ đọc

---

## Bước 8 – Tạo Controller `InventoryController.java`

`src/main/java/com/fudn/inventoryservice/controller/InventoryController.java`:

```java
package com.fudn.inventoryservice.controller;

import com.fudn.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(
            @RequestParam String skuCode,
            @RequestParam Integer quantity
    ) {
        return inventoryService.isInStock(skuCode, quantity);
    }
}
```

### TODO
- [x] Tạo class `InventoryController`
- [x] Endpoint `GET /api/inventory?skuCode=...&quantity=...` → 200, trả về `boolean`

---

## Bước 9 – Kiểm tra bằng Postman

```
GET http://localhost:8082/api/inventory?skuCode=iphone_15&quantity=100
```
→ Kỳ vọng: `true` (V2 migration đã thêm 100 cái iphone_15)

```
GET http://localhost:8082/api/inventory?skuCode=iphone_15&quantity=200
```
→ Kỳ vọng: `false` (chỉ có 100 cái, không đủ 200)

### TODO / Checklist Postman
- [x] `mvn spring-boot:run` chạy service thành công trên port 8082
- [x] GET với quantity=100 trả về `true`
- [x] GET với quantity=200 trả về `false`

---

## Bước 10 – Viết Integration Test

`src/test/java/com/fudn/inventoryservice/InventoryServiceApplicationTests.java`:

```java
package com.fudn.inventoryservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MySQLContainer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryServiceApplicationTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer("mysql:8.3.0");

    @LocalServerPort
    private Integer port;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        mySQLContainer.start();
    }

    @Test
    void shouldReadInventory() {
        var response = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=1")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Boolean.class);
        assertTrue(response);

        var negativeResponse = RestAssured.given()
                .when()
                .get("/api/inventory?skuCode=iphone_15&quantity=1000")
                .then()
                .log().all()
                .statusCode(200)
                .extract().response().as(Boolean.class);
        assertFalse(negativeResponse);
    }
}
```

Chạy test:
```bash
mvn test
```

### TODO
- [x] Tạo `InventoryServiceApplicationTests` (package `com.fudn.inventoryservice`)
- [x] Docker Desktop đang chạy (Testcontainers cần Docker)
- [x] `mvn test` PASS cả 2 assertion (true / false)

---

## ✅ Checklist hoàn thành Inventory Service

- [x] Cấu trúc thư mục đúng package `com.fudn.inventoryservice.*`
- [x] `init.sql` đã cập nhật, database `inventory_service` tồn tại trong container MySQL chung
- [x] `application.properties` trỏ đúng datasource, port 8082
- [x] Flyway migration `V1__init.sql` + `V2__add_inventory.sql` chạy thành công, có 4 SKU mẫu
- [x] `Inventory`, `InventoryRepository`, `InventoryService`, `InventoryController` đã tạo đủ
- [x] Postman test GET `/api/inventory` trả về đúng `true`/`false`
- [x] `mvn test` chạy Integration Test PASS

---

## Ghi chú thay đổi package

| Class | Package gốc | Package mới |
|---|---|---|
| Inventory | `com.programmingtechie.inventoryservice.model` | `com.fudn.inventoryservice.model` |
| InventoryRepository | `com.programmingtechie.inventoryservice.repository` | `com.fudn.inventoryservice.repository` |
| InventoryService | `com.programmingtechie.inventoryservice.service` | `com.fudn.inventoryservice.service` |
| InventoryController | `com.programmingtechie.inventoryservice.controller` | `com.fudn.inventoryservice.controller` |
| Test class | `com.programmingtechie.inventoryservice` | `com.fudn.inventoryservice` |

> **Lưu ý:** Đặt Group = `com.fudn` ngay từ khi generate project tại start.spring.io để thư mục package tự sinh đúng, tránh phải đổi thủ công.

---

## Tổng kết chung (cả 3 service)

| Service | Port | Database | Endpoint chính |
|---|---|---|---|
| product-service | 8080 | MongoDB | POST/GET `/api/product` |
| order-service | 8081 | MySQL | POST `/api/order` |
| inventory-service | 8082 | MySQL | GET `/api/inventory` |

Sau khi hoàn thành cả 3 file hướng dẫn (`product-service.md`, `order-service.md`, `inventory-service.md`), 3 service chạy **độc lập** với nhau. Ở Part 2, Order Service sẽ gọi Inventory Service qua Spring Cloud OpenFeign để kiểm tra tồn kho trước khi đặt hàng.
