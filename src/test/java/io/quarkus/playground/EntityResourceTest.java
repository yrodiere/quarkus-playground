package io.quarkus.playground;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import java.util.logging.Logger;

@QuarkusTest
public class EntityResourceTest {

    private static final Logger LOGGER = Logger.getLogger(EntityResource.class.getName());

    @Test
    public void test() {
        given()
                .when().put("/entity/1/")
                .then()
                .statusCode(204);
        
        
        given()
                .when().put("/entity/test/1/")
                .then()
                .statusCode(204);


    }

}
