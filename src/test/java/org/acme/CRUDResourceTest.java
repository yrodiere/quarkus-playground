package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class CRUDResourceTest {

    @Test
    public void test() {
        var response = given()
                .queryParam("value", "initial")
                .when().post("/crud");
        long id = Long.parseLong(response.getBody().asString());
        response.then()
                .statusCode(201);

        given()
                .when().get("/crud/{id}", id)
                .then()
                .statusCode(200)
                .body(is("initial"));

        given()
                .queryParam("value", "updated")
                .when().put("/crud/{id}", id)
                .then()
                .statusCode(204);

        given()
                .when().get("/crud/{id}", id)
                .then()
                .statusCode(200)
                .body(is("updated"));

        given()
                .queryParam("value", "updated")
                .when().delete("/crud/{id}", id)
                .then()
                .statusCode(204);

        given()
                .when().get("/crud/{id}", id)
                .then()
                .statusCode(404);
    }

}