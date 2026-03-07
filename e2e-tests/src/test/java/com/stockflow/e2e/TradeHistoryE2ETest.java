package com.stockflow.e2e;

import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.ElementsCollection;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;

@Slf4j
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

        log.info("Test setup complete - API: {}, UI: {}, username: {}", apiBaseUrl, Configuration.baseUrl, username);
    }

    @Test
    @Order(1)
    @DisplayName("Create user via REST API")
    void createUser() {
        log.info("Registering new user '{}' with email '{}'", username, email);

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

        log.info("User registered successfully with id={}", userId);
    }

    @Test
    @Order(2)
    @DisplayName("Login and navigate to portfolio")
    void loginAndGoToPortfolio() {
        log.info("Opening login page");
        open("/login");

        log.info("Entering credentials for user '{}'", username);
        $("[data-testid=input-username]").setValue(username);
        $("[data-testid=input-password]").setValue(PASSWORD);
        $("[data-testid=btn-submit]").click();

        log.info("Waiting for navigation to be visible");
        $("[data-testid=nav-portfolio]").shouldBe(visible);

        log.info("Navigating to portfolio page");
        $("[data-testid=nav-portfolio]").click();

        log.info("Verifying portfolio page loaded - Buy button visible");
        $("[data-testid=btn-buy]").shouldBe(visible);
    }

    @Test
    @Order(3)
    @DisplayName("Buy a stock and verify it appears in trade history")
    void buyStockAndVerifyHistory() {
        log.info("Opening buy trade modal");
        $("[data-testid=btn-buy]").click();

        log.info("Selecting first available stock symbol");
        $("[data-testid=trade-symbol]").shouldBe(visible);
        $("[data-testid=trade-symbol]").selectOption(0);

        log.info("Setting quantity to 5");
        $("[data-testid=trade-quantity]").clear();
        $("[data-testid=trade-quantity]").setValue("5");

        log.info("Confirming BUY trade");
        $("[data-testid=btn-confirm-trade]").click();

        log.info("Waiting for trade history to appear");
        $("[data-testid=trade-history]").shouldBe(visible);

        log.info("Verifying trade history contains at least 1 row");
        ElementsCollection rows = $$("[data-testid=trade-history-table] tbody tr");
        rows.shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual(1));

        log.info("Verifying first row shows BUY trade with quantity 5");
        rows.get(0).shouldHave(text("BUY"));
        rows.get(0).shouldHave(text("5"));

        log.info("BUY trade verified successfully in trade history");
    }

    @Test
    @Order(4)
    @DisplayName("Sell a stock and verify both trades appear in history")
    void sellStockAndVerifyHistory() {
        log.info("Opening sell trade modal");
        $("[data-testid=btn-sell]").click();

        log.info("Selecting first available stock symbol");
        $("[data-testid=trade-symbol]").shouldBe(visible);
        $("[data-testid=trade-symbol]").selectOption(0);

        log.info("Setting quantity to 2");
        $("[data-testid=trade-quantity]").clear();
        $("[data-testid=trade-quantity]").setValue("2");

        log.info("Confirming SELL trade");
        $("[data-testid=btn-confirm-trade]").click();

        log.info("Waiting for trade history to update");
        $("[data-testid=trade-history]").shouldBe(visible);

        log.info("Verifying trade history contains at least 2 rows");
        ElementsCollection rows = $$("[data-testid=trade-history-table] tbody tr");
        rows.shouldHave(com.codeborne.selenide.CollectionCondition.sizeGreaterThanOrEqual(2));

        log.info("Verifying both BUY and SELL trades are present");
        $("[data-testid=trade-history-table]").shouldHave(text("BUY"));
        $("[data-testid=trade-history-table]").shouldHave(text("SELL"));

        log.info("Verifying trade quantities (5 and 2) are displayed");
        $("[data-testid=trade-history-table]").shouldHave(text("5"));
        $("[data-testid=trade-history-table]").shouldHave(text("2"));

        log.info("SELL trade verified successfully - both trades visible in history");
    }

    @AfterAll
    static void tearDown() {
        log.info("Closing browser");
        closeWebDriver();
    }
}
