package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ExampleResourceTest {
	@Test
	void get() {
		given()
				.when().get( "/fruits" )
				.then()
				.statusCode( 200 )
				.body( is( "[]" ) );
	}

	@Test
	void post() {
		given()
				.body( "{\"name\": \"potato\"}" )
				.contentType( ContentType.JSON )
				.when().post( "/fruits" )
				.then()
				.statusCode( 201 );
	}

}
