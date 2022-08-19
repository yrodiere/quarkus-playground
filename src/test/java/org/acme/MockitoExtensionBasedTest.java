package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@QuarkusTest
@ExtendWith(MockitoExtension.class)
public class MockitoExtensionBasedTest {

	@Test
	void testWithMock() {
		MyService mock = Mockito.mock( MyService.class);
		QuarkusMock.installMockForType(mock, MyService.class);

		Mockito.when(mock.greet("Jane")).thenReturn("A mock for Jane");
        given()
        	.when().get("/hello/Jane")
        	.then()
        		.statusCode(200)
        		.body(is("A mock for Jane"));
		Mockito.verifyNoMoreInteractions(mock);
	}

}
