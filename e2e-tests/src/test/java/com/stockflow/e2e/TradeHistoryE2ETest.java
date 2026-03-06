package com.stockflow.e2e;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TradeHistoryE2ETest {

    private static String username;
    private static String email;
    private static final String PASSWORD = "Test1234!";
    private static int userId;
    private static String apiBaseUrl;

    @BeforeAll
    static void setUp() {
        apiBaseUrl = System.getProperty("app.api.baseUrl", "http://localhost:8085");
        Configuration.baseUrl = System.getProperty("selenide.baseUrl", "http://localhost:4200");
        Configuration.browser = "chrome";
        Configuration.headless = false;
        Configuration.timeout = 10000;

        RestAssured.baseURI = apiBaseUrl;

        String unique = UUID.randomUUID().toString().substring(0, 8);
        username = "e2etrade_" + unique;
        email = "e2etrade_" + unique + "@test.com";
    }

    @Test
    @Order(1)
    @DisplayName("Create user via REST API")
    void createUser() {
        userId = given()
                .contentType(ContentType.JSON)
                .body(Map.of(
                        "username", username,
                        "email", email,
                        "password", PASSWORD
                ))
                .when()
                .post("/users/register")
                .then()
                .statusCode(201)
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("Login and navigate to portfolio")
    void loginAndGoToPortfolio() {
        open("/login");

        $("[data-testid=input-username]").setValue(username);
        $("[data-testid=input-password]").setValue(PASSWORD);
        $("[data-testid=btn-submit]").click();

        $("[data-testid=nav-portfolio]").shouldBe(visible);
        $("[data-testid=nav-portfolio]").click();

        $("[data-testid=btn-buy]").shouldBe(visible);
    }

    @Test
    @Order(3)
    @DisplayName("Buy a stock and verify it appears in trade history")
    void buyStockAndVerifyHistory() {
        $("[data-testid=btn-buy]").click();

        $("[data-testid=trade-symbol]").shouldBe(visible);
        $("[data-testid=trade-symbol]").selectOption(0);
        $("[data-testid=trade-quantity]").clear();
        $("[data-testid=trade-quantity]").setValue("5");
        $("[data-testid=btn-confirm-trade]").click();

        // Wait for modal to close and trade history to appear
        $("[data-testid=trade-history]").shouldBe(visible);

        // Verify the buy trade row exists
        ElementsCollection rows = $$("[data-testid=trade-history-table] tbody tr");
        rows.shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual(1));

        // First row should be the latest trade (BUY, qty 5)
        rows.get(0).shouldHave(text("BUY"));
        rows.get(0).shouldHave(text("5"));
    }

    @Test
    @Order(4)
    @DisplayName("Sell a stock and verify both trades appear in history")
    void sellStockAndVerifyHistory() {
        $("[data-testid=btn-sell]").click();

        $("[data-testid=trade-symbol]").shouldBe(visible);
        $("[data-testid=trade-symbol]").selectOption(0);
        $("[data-testid=trade-quantity]").clear();
        $("[data-testid=trade-quantity]").setValue("2");
        $("[data-testid=btn-confirm-trade]").click();

        // Wait for trade history to update with the new SELL row
        $("[data-testid=trade-history]").shouldBe(visible);

        // Should now have at least 2 trades
        ElementsCollection rows = $$("[data-testid=trade-history-table] tbody tr");
        rows.shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual(2));

        // Verify that both a BUY and a SELL trade exist in the table
        $("[data-testid=trade-history-table]").shouldHave(text("BUY"));
        $("[data-testid=trade-history-table]").shouldHave(text("SELL"));

        // Verify quantities are present
        $("[data-testid=trade-history-table]").shouldHave(text("5"));
        $("[data-testid=trade-history-table]").shouldHave(text("2"));
    }

    @AfterAll
    static void tearDown() {
        closeWebDriver();
    }
}
