package hexlet.code;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import hexlet.code.model.query.QUrlCheck;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import jakarta.servlet.http.HttpServletResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assertions;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public final class AppTest {

    @Test
    public void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static final String EXPECTED_URL = "https://www.example.com";
    private static final String CORRECT_URL = "https://www.google.com";
    private static final String URL_FOR_NON_EXISTING_ENTITY_TEST = "https://www.dzen.ru";
    private static final String WRONG_URL = "www.ussr.su";
    private static Database database;

    @BeforeAll
    public static void beforeAll() {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    public void beforeEach() {
        database.script().run("/truncate.sql");
        Url url = new Url(CORRECT_URL);
        url.save();
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
        public void testShowUrlById() {

            Url actualUrl = new QUrl()
                    .name.equalTo(CORRECT_URL)
                    .findOne();

            long id = actualUrl.getId();

            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/" + id).asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
            assertThat(body).contains(CORRECT_URL);

            Url wrongUrl = new Url(URL_FOR_NON_EXISTING_ENTITY_TEST);
            wrongUrl.save();
            long idForDeletion = Objects.requireNonNull(new QUrl()
                            .name.eq(URL_FOR_NON_EXISTING_ENTITY_TEST)
                            .findOne()).getId();

            wrongUrl.delete();
            response = Unirest.get(baseUrl + "/urls/" + idForDeletion).asString();
            assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Nested
    class UrlCheckControllerTest {

        @Test
        public void addUrlCheckTest() {

            Unirest.post(baseUrl + "/urls")
                    .field("url", CORRECT_URL)
                    .asEmpty();

            Url actualUrl = new QUrl()
                    .name.equalTo(CORRECT_URL)
                    .findOne();

            long id = actualUrl.getId();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(CORRECT_URL);

            String urlForPostMethod = baseUrl + "/urls/" + id + "/checks";

            Unirest.post(urlForPostMethod).asEmpty();

            UrlCheck actualCheckUrl = new QUrlCheck()
                    .url.equalTo(actualUrl)
                    .createdAt.desc()
                    .findOne();

            assertThat(actualCheckUrl).isNotNull();
            assertThat(actualCheckUrl.getStatusCode()).isEqualTo(200);
            assertThat(actualCheckUrl.getTitle()).isEqualTo("Google");
            assertThat(actualCheckUrl.getH1())
                    .isEqualTo("");
            assertThat(actualCheckUrl.getDescription()).contains("");
        }

    }


}
