package com.fudn.orderservice;

import com.fudn.orderservice.repository.OrderRepository;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import javax.sql.DataSource;

import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.enabled=false",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect"
        })
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "inventory.url"))
class SwaggerIntegrationTest {

    @LocalServerPort
    private Integer port;

    @MockitoBean
    private DataSource dataSource;

    @MockitoBean
    private OrderRepository orderRepository;

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
                .body("info.title", equalTo("Order Service API"))
                .body("info.version", equalTo("v0.0.1"));
    }

    @Test
    void apiDocsShouldContainOrderEndpoints() {
        RestAssured.given()
                .when()
                .get("/api-docs")
                .then()
                .statusCode(200)
                .body("paths", notNullValue());
    }
}
