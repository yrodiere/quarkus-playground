package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MyEntityResourceTest {

    @Test
    public void test() {
        given()
          .when().get("/myentity/count")
          .then()
             .statusCode(200)
             .body(is("3"));
        given()
                .when().post("/myentity/create")
                .then()
                .statusCode(200);
        given()
                .when().get("/myentity/count")
                .then()
                .statusCode(200)
                .body(is("4"));
    }

}