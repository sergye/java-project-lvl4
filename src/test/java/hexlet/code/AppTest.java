package hexlet.code;

import hexlet.code.model.Url;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl;
    private static Transaction transaction;
    private static MockWebServer mockWebServer;

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();

        baseUrl = "http://localhost:" + port;

        existingUrl = new Url("https://ru.hexlet.io");
        existingUrl.save();

        mockWebServer = new MockWebServer();
        String expected = Files.readString(Paths.get("src", "test", "resources", "testpage.html"));
        mockWebServer.enqueue(new MockResponse().setBody(expected));
        mockWebServer.start();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        transaction = DB.beginTransaction();
    }

    @AfterEach
    void afterEach() {
        transaction.rollback();
    }

    @Nested
    class UrlControllerTest {

        @Test
        void testAddUrl() {
            String inputUrl = "https://edition.cnn.com/search?q=europe";
            String outputUrl = "https://edition.cnn.com";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls").field("url", inputUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls");

            response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(outputUrl);
            assertThat(body).contains("Страница успешно добавлена");

        }

        @Test
        void testAddExistingUrl() {
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", existingUrl.getName()).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/urls");

            response = Unirest.get(baseUrl).asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains("Страница уже существует");
        }

        @Test
        void testAddInvalidUrl() {
            String inputUrl = "Abracadabra";
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls").field("url", inputUrl).asEmpty();

            assertThat(response.getStatus()).isEqualTo(302);
            assertThat(response.getHeaders().getFirst("Location")).isEqualTo("/");

            response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).doesNotContain(inputUrl);
            assertThat(body).contains("Некорректный URL");

        }

        @Test
        void testGetUrls() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());
        }

        @Test
        void testGetUrl() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + existingUrl.getId()).asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(existingUrl.getName());
        }
        @Test
        void testCheckUrl() {
            String description = "Lorem ipsum dolor sit amet.";
            String title = "Test page title";
            String h1 = "Test page h1 header";

            String mockUrl = mockWebServer.url("/").toString();
            HttpResponse<String> response = Unirest.get(mockUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
        }
    }
}
