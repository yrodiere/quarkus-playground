package org.acme;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class MyEntityResourceTest {

    @Test
    public void testUuid() {
        String writtenUuid = given()
                .queryParam("field", "foo")
                .when().post("/myentity")
                .then()
                .statusCode(200)
                .extract().body().asString();

        String readUuid = given()
                .queryParam("field", "foo")
                .when().get("/myentity")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(readUuid).isEqualTo(writtenUuid);

        String nativeReadUuid = given()
                .queryParam("field", "foo")
                .when().get("/myentity/native")
                .then()
                .statusCode(200)
                .extract().body().asString();
        assertThat(nativeReadUuid).isEqualTo(writtenUuid);
    }

}