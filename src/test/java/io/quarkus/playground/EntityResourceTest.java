package io.quarkus.playground;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
public class EntityResourceTest {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

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
        assertThat(containedInitialized).isEqualTo("false");

    }

    @Test
    public void test2() {
        given()
                .when().put("/entity/2/")
                .then()
                .statusCode(204);

        String containedInitializedAfterMerge = given()
                .when().get("/entity/2/is-contained-initialized-after-merge")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(containedInitializedAfterMerge).isEqualTo("false");
    }

    @Test
    public void test3() {

        String containedInitializedAfterMerge = given()
                .when().get("/entity/1/lazy-exception")
                .then()
                .statusCode(200)
                .extract().body().asString();
        LOGGER.log(Level.SEVERE, "BODY: " +containedInitializedAfterMerge);

    }

}
