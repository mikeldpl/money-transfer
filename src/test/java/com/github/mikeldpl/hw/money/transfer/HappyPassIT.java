package com.github.mikeldpl.hw.money.transfer;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import java.math.BigDecimal;

import com.jayway.restassured.RestAssured;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;

public class HappyPassIT {

    @ClassRule
    public static GenericContainer appContainer = new GenericContainer<>("mikeldpl/moneytransfer")
            .withCreateContainerCmdModifier(cmd -> cmd.withName("moneytransfer-container-test"))
            .withExposedPorts(4567)
            .withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger("Container")))
            .waitingFor(Wait.forHttp("/money-transfer/1/accounts"));

    private String baseUrl;

    @Before
    public void setUp() {
        this.baseUrl = "http://" + appContainer.getContainerIpAddress() + ":" + appContainer.getFirstMappedPort() + "/money-transfer/1";
    }

    @Test
    public void createAccount_success() {
        RestAssured.given()
                .baseUri(baseUrl)
                .body("{\"name\": \"account1\", \"money\":\"15.25\"}")
            .when()
                .post("/accounts")
            .then()
                .assertThat()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    public void createTransfer_success() {
        BigDecimal transferAmount = BigDecimal.valueOf(4);
        BigDecimal account1Money = BigDecimal.valueOf(10);
        BigDecimal account2Money = BigDecimal.valueOf(1);

       Long account1Id = createAccount(account1Money);
       Long account2Id = createAccount(account2Money);

        //creates Transfer: account 1 send money to account 2
        RestAssured.given().baseUri(baseUrl)
                .body("{\"receiver_account_id\": " + account2Id + ", \"amount\": " + transferAmount + "}")
            .when()
                .post("/accounts/{id}/transfers", account1Id)
            .then()
                .assertThat()
                .statusCode(201)
                .body("id", notNullValue())
                .body("status", equalTo("PROCESSING"))
                .body("amount", equalTo(transferAmount.floatValue()));

        //account1.money = account1Money - transferAmount
        double resultAccount1Money = account1Money.subtract(transferAmount).doubleValue();
        double updatedMoney1 = getMoneyByAccountId(account1Id);
        Assert.assertThat(updatedMoney1, equalTo(resultAccount1Money));
        //account2.money is not changed until transfer is not approved.
        double updatedMoney2 = getMoneyByAccountId(account2Id);
        Assert.assertThat(updatedMoney2, equalTo(account2Money.doubleValue()));
    }

    private double getMoneyByAccountId(Long account1Id) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .get("/accounts/{id}", account1Id)
                .body().jsonPath().getDouble("money");
    }

    @Test
    public void rejectTransfer_success() {
        BigDecimal transferAmount = BigDecimal.valueOf(2);
        BigDecimal account1Money = BigDecimal.valueOf(10);
        BigDecimal account2Money = BigDecimal.valueOf(2);

        Long account1Id = createAccount(account1Money);
        Long account2Id = createAccount(account2Money);
        Long transferId = createTransfer(account1Id, account2Id, transferAmount);

        RestAssured.given().baseUri(baseUrl)
                .body("{\"reason\": \"need money\", \"next_status\":\"REJECTED\"}")
            .when()
                .post("/accounts/{account_id}/transfers/{transfer_id}/actions", account1Id, transferId)
            .then()
                .assertThat()
                .statusCode(201)
                .body("id", notNullValue());


        double updatedMoney1 = getMoneyByAccountId(account1Id);
        Assert.assertThat(updatedMoney1, equalTo(account1Money.doubleValue()));
        double updatedMoney2 = getMoneyByAccountId(account2Id);
        Assert.assertThat(updatedMoney2, equalTo(account2Money.doubleValue()));
    }

    @Test
    public void approveTransfer_success() {
        BigDecimal transferAmount = BigDecimal.valueOf(2);
        BigDecimal account1Money = BigDecimal.valueOf(2);
        BigDecimal account2Money = BigDecimal.valueOf(8);

        Long account1Id = createAccount(account1Money);
        Long account2Id = createAccount(account2Money);
        Long transferId = createTransfer(account1Id, account2Id, transferAmount);

        RestAssured.given().baseUri(baseUrl)
                .body("{\"reason\": \"need money\", \"next_status\":\"APPROVED\"}")
            .when()
                .post("/accounts/{account_id}/transfers/{transfer_id}/actions", account1Id, transferId)
            .then()
                .assertThat()
                .statusCode(201)
                .body("id", notNullValue());


        //account1.money = account1Money - transferAmount
        double resultAccount1Money = account1Money.subtract(transferAmount).doubleValue();
        double updatedMoney1 = getMoneyByAccountId(account1Id);
        Assert.assertThat(updatedMoney1, equalTo(resultAccount1Money));
        //account2.money = account2Money + transferAmount
        double resultAccount2Money = account2Money.add(transferAmount).doubleValue();
        double updatedMoney2 = getMoneyByAccountId(account2Id);
        Assert.assertThat(updatedMoney2, equalTo(resultAccount2Money));
    }

    private Long createAccount(BigDecimal accountMoney) {
        return RestAssured.given().baseUri(baseUrl)
                .body("{\"name\": \"name\", \"money\":\"" + accountMoney + "\"}")
                .post("/accounts")
                .body().jsonPath().getLong("id");
    }

    private Long createTransfer(Long account1Id, Long account2Id, BigDecimal transferAmount) {
        return RestAssured.given().baseUri(baseUrl)
                .body("{\"receiver_account_id\": " + account2Id + ", \"amount\": " + transferAmount + "}")
                .when()
                .post("/accounts/{id}/transfers", account1Id)
                .body().jsonPath().getLong("id");

    }
}
