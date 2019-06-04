package com.github.mikeldpl.hw.money.transfer;

import com.jayway.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class ApiHappyPassIT {

    @ClassRule
    public static GenericContainer appContainer = new GenericContainer<>("mikeldpl/moneytransfer")
            .withCreateContainerCmdModifier(cmd -> cmd.withName("moneytransfer-container-test"))
            .withExposedPorts(4567)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Container")))
            .waitingFor(Wait.forHttp("/money-transfer/1/accounts"));

    private String baseUrl;

    @Before
    public void setUp() {
        this.baseUrl = "http://" + appContainer.getContainerIpAddress() + ":" + appContainer.getFirstMappedPort() + "/moneytransfer/1";
    }

    @Test
    public void createAccount() {
        RestAssured.given()
                .baseUri(baseUrl)
                .body("{\"name\": \"account1\", \"money\":\"15.25\"}")
            .when()
                .post("/accounts")
            .then()
                .assertThat()
                .statusCode(201)
                .body("id", Matchers.notNullValue());
    }
}
