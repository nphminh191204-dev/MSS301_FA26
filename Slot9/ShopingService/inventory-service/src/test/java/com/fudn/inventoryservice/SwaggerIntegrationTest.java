package com.fudn.inventoryservice;

import com.fudn.inventoryservice.repository.InventoryRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        FlywayAutoConfiguration.class
})
class SwaggerIntegrationTest {

    @LocalServerPort
    private Integer port;

    @MockBean
    private InventoryRepository inventoryRepository;

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    void swaggerUiShouldBeAccessible() {
        RestAssured.given()
                .when()
                .get("/swagger-ui.html")
                .then()
                .statusCode(anyOf(equalTo(200), equalTo(302)));
    }

    @Test
    void apiDocsShouldReturnJson() {
        RestAssured.given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("info.title", equalTo("Inventory Service API"))
                .body("info.version", equalTo("v0.0.1"));
    }

    @Test
    void apiDocsShouldContainInventoryEndpoints() {
        RestAssured.given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("paths", notNullValue());
    }
}
