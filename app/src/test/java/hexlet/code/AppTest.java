package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;

import io.javalin.Javalin;
import jakarta.servlet.http.HttpServletResponse;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.h2.engine.Database;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    @Test
    public void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static final String CORRECT_URL = "https://www.google.com";
    private static final String URL_FOR_NON_EXISTING_ENTITY_TEST = "https://www.dzen.ru";
    private static final String WRONG_URL = "www.ussr.su";
    private static Database database;

    @BeforeAll
    public static void beforeAll() throws SQLException, IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public void beforeEach() throws SQLException {
        UrlRepository.truncateDB();
        UrlCheckRepository.truncateDB();

        Url firstUrl = new Url(CORRECT_URL, new Timestamp(System.currentTimeMillis()));
        UrlRepository.save(firstUrl);
    }

    @Test
    public void testWelcome() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        int status = response.getStatus();
        assertThat(status).isEqualTo(HttpServletResponse.SC_OK);
    }

    @Nested
    class UrlControllerTest {

        @Test
        public void testCreateUrl() {
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", CORRECT_URL)
                    .asString();

            int postQueryStatus = response.getStatus();
            Assertions.assertEquals(postQueryStatus, HttpServletResponse.SC_FOUND);

            response = Unirest.get(baseUrl + "/urls").asString();

            int getQueryStatus = response.getStatus();
            String responseBody = response.getBody();

            assertThat(getQueryStatus).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(responseBody).contains(CORRECT_URL);
        }

        @Test
        public void testCreateWrongUrl() {
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", WRONG_URL)
                    .asString();

            int postQueryStatus = response.getStatus();
            assertThat(postQueryStatus).isEqualTo(HttpServletResponse.SC_FOUND);

            response = Unirest.get(baseUrl).asString();
            assertThat(response.getBody()).contains("Некорректный URL");

            response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getBody()).doesNotContain(WRONG_URL);
        }

        @Test
        public void testShowUrls() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();
            int getQueryStatus = response.getStatus();

            assertThat(getQueryStatus).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(CORRECT_URL);
        }

        @Test
        public void testShowUrlById() throws SQLException {

            Url actualUrl = UrlRepository.findByName(CORRECT_URL).orElseThrow(
                    () -> new SQLException("url with the name " + CORRECT_URL + " was not found!"));

            Long id = actualUrl.getId();

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(CORRECT_URL,
                    actualUrl.getCreatedAt()
                            .toLocalDateTime()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            Url wrongUrl = new Url(URL_FOR_NON_EXISTING_ENTITY_TEST, new Timestamp(System.currentTimeMillis()));
            UrlRepository.save(wrongUrl);
            Long idForDeletion = UrlRepository.findByName(URL_FOR_NON_EXISTING_ENTITY_TEST)
                    .orElseThrow(() -> new SQLException("wrongUrl with name " + URL_FOR_NON_EXISTING_ENTITY_TEST
                                    + " was not found in DB!"))
                    .getId();

            UrlRepository.delete(idForDeletion);
            response = Unirest.get(baseUrl + "/urls/" + idForDeletion).asString();
            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    class UrlCheckControllerTest {

        @Test
        public void addUrlCheckTest() throws SQLException {

            Unirest.post(baseUrl + "/urls")
                    .field("url", CORRECT_URL)
                    .asEmpty();

            Url actualUrl = UrlRepository.findByName(CORRECT_URL)
                    .orElseThrow(() -> new SQLException("Url with name " + CORRECT_URL + " was not found"));

            Long id = actualUrl.getId();

            assertThat(actualUrl.getName()).isEqualTo(CORRECT_URL);

            String urlForPostMethod = baseUrl + "/urls/" + id + "/checks";

            Unirest.post(urlForPostMethod).asEmpty();

            UrlCheck actualCheckUrl = UrlCheckRepository.findLastCheckByUrlId(id)
                    .orElseThrow(() -> new SQLException("The last check for url with id " + id + " was not found"));

            assertThat(actualCheckUrl.getStatusCode()).isEqualTo(200);
            assertThat(actualCheckUrl.getTitle()).isEqualTo("Google");
            assertThat(actualCheckUrl.getH1())
                    .isEqualTo("");
            assertThat(actualCheckUrl.getDescription()).contains("");
        }
    }
}
