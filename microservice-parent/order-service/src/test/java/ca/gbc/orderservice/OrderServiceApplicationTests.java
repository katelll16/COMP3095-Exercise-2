package ca.gbc.orderservice;

import ca.gbc.orderservice.stub.InventoryClientStub;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import static org.hamcrest.MatcherAssert.assertThat;

// Tells Springboot to look for a main configuration class (SpringBootApplication)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderServiceApplicationTests {

    //This annotation is used in combination with Testconatiners to automatically configure the connection to
    //the Test MongoDBContainer
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:15-alpine");

    @LocalServerPort
    private Integer port;

    //https://localhost:port/api/product

    @BeforeEach
    void setup(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    static {
        postgreSQLContainer.start();
    }

    @Test
    void shouldSubmitOrder(){

        String submitOrderJson = """
               {
                 "skuCode": "samsung_tv_2024",
                 "price": 2000,
                 "quantity": 10
               }
            """;

        InventoryClientStub.stubInventoryCall("sumsung_tv_2024", 10);

        var responseBodyString = RestAssured.given()
                .contentType("application/json")
                .body(submitOrderJson)
                .when()
                .post("/api/order")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .body().asString();

        assertThat(responseBodyString, Matchers.is("Order Placed Successfully"));
    }


}