package org.acme.resteasy;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class ExampleResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when()
          	.get("/resteasy/hello")
          .then()
             .statusCode(200)
             .body(is("hello"));
    }

}