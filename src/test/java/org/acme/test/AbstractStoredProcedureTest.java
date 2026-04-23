package org.acme.test;

import io.restassured.http.ContentType;
import org.acme.scaffolding.DatabaseProfile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;

public abstract class AbstractStoredProcedureTest {

    protected abstract String getEndpointRoot();

    @BeforeEach
    public void resetData() {
        given()
                .when().post("/test-data/reset")
                .then()
                .statusCode(200);
    }

    @Test
    public void testEffectiveProfile() {
        given()
                .when().get(getEndpointRoot() + "/profile")
                .then()
                .statusCode(200)
                .body(is(DatabaseProfile.current().name));
    }

    @Test
    public void testCallNoParams() {
        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(0));

        given()
                .when().post(getEndpointRoot() + "/no-params")
                .then()
                .statusCode(200);

        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    public void testCallWithInputParams() {
        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(0));

        given()
                .queryParam("username", "alice")
                .when().post(getEndpointRoot() + "/input-params")
                .then()
                .statusCode(200);

        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .body("[0].profile.username", is("alice"));
    }

    @Test
    public void testCallWithOutputParams() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/output-params")
                .then()
                .statusCode(200)
                .body(Matchers.is("1"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/output-params")
                .then()
                .statusCode(200)
                .body(Matchers.is("2"));
    }

    @Test
    public void testCallReturningDataAsResultSet() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-result-set")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1))
                .body("username", hasItems("bob"))
                .body("fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-result-set")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallReturningDataAsBasicType() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("1"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("2"));
    }

    @Test
    public void testCallReturningDataAsEntitiesNoAssociation() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1))
                .body("username", hasItems("bob"))
                .body("fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallReturningDataAsEntitiesToOne() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1))
                .body("profile.username", hasItems("bob"))
                .body("profile.fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/return-data-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("profile.username", hasItems("bob", "charlie"))
                .body("profile.fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallWithCursor() {
        // TODO also test using this as a true cursor (multiple batches with flush/clear?)
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/cursor")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }
}
