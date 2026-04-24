package org.acme.test;

import io.restassured.http.ContentType;
import org.acme.scaffolding.DatabaseProfile;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
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
    public void testCallProcedureWithoutParams() {
        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(0));

        given()
                .when().post(getEndpointRoot() + "/procedure/without-params")
                .then()
                .statusCode(200);

        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(1));
    }

    @Test
    public void testCallProcedureWithInputParams() {
        given()
                .when().get("/test-data/activities")
                .then()
                .statusCode(200)
                .body("size()", is(0));

        given()
                .queryParam("username", "alice")
                .when().post(getEndpointRoot() + "/procedure/with-input-params")
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
    public void testCallProcedureWithOutputParamBasicType() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("1"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("2"));
    }

    // ===== Functions (with return values) =====

    @Test
    public void testCallFunctionReturningBasicType() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("1"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-basic-type")
                .then()
                .statusCode(200)
                .body(Matchers.is("2"));
    }

    @Test
    public void testCallFunctionReturningTuples() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-tuples")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("username", hasItems("bob"))
                .body("fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-tuples")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallFunctionReturningEntitiesNoAssociation() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("username", hasItems("bob"))
                .body("fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallFunctionReturningEntitiesWithToOne() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("profile.username", hasItems("bob"))
                .body("profile.fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/function/return-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("profile.username", hasItems("bob", "charlie"))
                .body("profile.fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    // ===== Procedures (with output parameters) =====

    @Test
    public void testCallProcedureWithOutputParamTuples() {
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
                .when().get(getEndpointRoot() + "/procedure/output-param-tuples")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallProcedureWithOutputParamEntitiesNoAssociation() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("username", hasItems("bob"))
                .body("fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-entities-no-association")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("username", hasItems("bob", "charlie"))
                .body("fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }

    @Test
    public void testCallProcedureWithOutputParamEntitiesWithToOne() {
        given()
                .queryParam("username", "bob")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(1))
                .body("profile.username", hasItems("bob"))
                .body("profile.fullName", hasItems("Bob Johnson"));

        given()
                .queryParam("username", "charlie")
                .when().post("/test-data/add-activity")
                .then()
                .statusCode(200);

        given()
                .when().get(getEndpointRoot() + "/procedure/output-param-entities-toone")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", is(2))
                .body("profile.username", hasItems("bob", "charlie"))
                .body("profile.fullName", hasItems("Bob Johnson", "Charlie Brown"));
    }
}
