package com.stockflow.e2e;

import com.codeborne.selenide.Configuration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.UUID;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserProfileE2ETest {

    private static String username;
    private static String email;
    private static final String PASSWORD = "Test1234!";
    private static int userId;

    @BeforeAll
    static void setUp() {
        String apiBaseUrl = System.getProperty("app.api.baseUrl", "http://localhost:8085");
        Configuration.baseUrl = System.getProperty("selenide.baseUrl", "http://localhost:4200");
        Configuration.browser = "chrome";
        Configuration.headless = false;
        Configuration.timeout = 10000;

        RestAssured.baseURI = apiBaseUrl;

        String unique = UUID.randomUUID().toString().substring(0, 8);
        username = "e2euser_" + unique;
        email = "e2euser_" + unique + "@test.com";
    }

    @Test
    @Order(1)
    @DisplayName("Create user via REST API")
    void createUserViaApi() {
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
                .body("id", notNullValue())
                .extract()
                .path("id");
    }

    @Test
    @Order(2)
    @DisplayName("Login, navigate to profile, and verify user details")
    void loginAndVerifyProfile() {
        open("/login");

        $("[data-testid=input-username]").setValue(username);
        $("[data-testid=input-password]").setValue(PASSWORD);
        $("[data-testid=btn-submit]").click();

        // Wait for redirect to dashboard
        $("[data-testid=nav-profile]").shouldBe(visible);

        // Navigate to profile
        $("[data-testid=nav-profile]").click();

        // Assert profile fields
        $("[data-testid=profile-username]").shouldHave(text(username));
        $("[data-testid=profile-email]").shouldHave(text(email));
        $("[data-testid=profile-userid]").shouldHave(text(String.valueOf(userId)));
        $("[data-testid=profile-member-since]").shouldNotBe(empty);
    }

    @AfterAll
    static void tearDown() {
        closeWebDriver();
    }
}
