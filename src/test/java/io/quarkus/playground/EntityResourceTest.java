package io.quarkus.playground;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EntityResourceTest {

    @Test
    public void test() {
        given()
                .when().put("/entity/1/")
                .then()
                .statusCode(204);

        String containedInitialized = given()
                .when().get("/entity/1/is-contained-initialized")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat( containedInitialized ).isEqualTo("false");
    }

}