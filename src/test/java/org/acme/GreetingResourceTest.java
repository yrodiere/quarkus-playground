package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/hello")
          .then()
             .statusCode(200)
             .body(is("Hello from RESTEasy Reactive"));
    }

    @Test
    public void testGetByIdEndpoint() {
        MyEntity deserializedEntity = given()
                .when().get("/hello/1")
                .then()
                .statusCode(200)
                .extract().body().as(MyEntity.class);
        assertThat(deserializedEntity).isNotNull();
        assertThat(deserializedEntity.getField()).isEqualTo("field-1");
    }

}