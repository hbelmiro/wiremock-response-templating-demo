package org.acme;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.extension.responsetemplating.ResponseTemplateTransformer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class ResponseTemplatingTest {

    private WireMockServer server;

    @BeforeEach
    void setUp() {
        server = new WireMockServer(WireMockConfiguration.wireMockConfig().extensions(new ResponseTemplateTransformer(false)).dynamicPort());
        server.start();
    }

    @Test
    void test() {
        mockWebServer();

        String message = "hello world";

        String responseBody = given()
                                      .contentType(ContentType.JSON)
                                      .accept(ContentType.JSON)
                                      .body(Collections.singletonMap("message", message))
                                      .post("http://localhost:" + server.port() + "/my/resource")
                                      .then()
                                      .extract().body().asString();

        assertEquals(message, responseBody);
    }

    private void mockWebServer() {
        server.stubFor(
                post("/my/resource")
                    .willReturn(ok()
                    .withBody("{{jsonPath request.body '$.message'}}")
                    .withTransformers("response-template")));
    }

    @AfterEach
    void tearDown() {
        server.shutdownServer();
    }
}