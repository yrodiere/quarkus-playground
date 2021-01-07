package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testStatic() {
        given()
          .when().get("/hello/static")
          .then()
             .statusCode(200)
             .body(is("foo1() was called!"));
    }

    @Test
    public void testRuntime() {
        given()
                .when().get("/hello/runtime")
                .then()
                .statusCode(200)
                .body(is("foo3() was called!"));
    }

    @Test
    public void testStaticRuntime() {
        given()
                .when().get("/hello/static-runtime")
                .then()
                .statusCode(200)
                .body(is("foo1() was called!"));
    }

    @Test
    public void testStaticAmbiguousCallSite() {
        given()
                .when().get("/hello/static-ambiguous-callsite?method=foo1")
                .then()
                .statusCode(200)
                .body(is("foo1() was called!"));
        given()
                .when().get("/hello/static-ambiguous-callsite?method=foo2")
                .then()
                .statusCode(200)
                .body(is("foo2() was called!"));
    }

}