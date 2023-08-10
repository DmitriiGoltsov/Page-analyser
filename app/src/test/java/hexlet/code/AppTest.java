package hexlet.code;

import hexlet.code.model.Url;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

public class AppTest {

    @Test
    public void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static final String EXPECTED_URL = "https://www.example.com";
    private static final String CORRECT_URL = "https://dzen.ru/";
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
        Url url = new Url(EXPECTED_URL);
        url.save();
    }

    @Test
    public void testWelcome() {
        HttpResponse<String> response = Unirest.get(baseUrl).asString();
        int status = response.getStatus();
        assertThat(status).isEqualTo(200);
    }

    @Nested
    class UrlControllerTest {

        @Test
        public void testCreateUrl() {
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", CORRECT_URL)
                    .asString();

            int postQueryStatus = response.getStatus();
            Assertions.assertEquals(postQueryStatus, 302);

            response = Unirest.get(baseUrl + "/urls").asString();

            int getQueryStatus = response.getStatus();
            String responseBody = response.getBody();

            assertThat(getQueryStatus).isEqualTo(200);
            assertThat(responseBody).contains("Страница успешно добавлена");
            assertThat(responseBody).contains(CORRECT_URL);
        }

        @Test
        public void testCreateWrongUrl() {
            HttpResponse<String> response = Unirest.post(baseUrl + "/urls")
                    .field("url", WRONG_URL)
                    .asString();

            int postQueryStatus = response.getStatus();
            assertThat(postQueryStatus).isEqualTo(302);

            response = Unirest.get(baseUrl).asString();
            assertThat(response.getBody()).contains("Некорректный URL");

            response = Unirest.get(baseUrl + "/urls").asString();
            assertThat(response.getBody()).doesNotContain(WRONG_URL);
        }

        @Test
        public void testShowURLs() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls").asString();
            String body = response.getBody();
            int getQueryStatus = response.getStatus();

            assertThat(getQueryStatus).isEqualTo(200);
            assertThat(body).contains(EXPECTED_URL);
        }

        @Test
        public void testShowUrlById() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(body).contains(EXPECTED_URL);

            response = Unirest.get(baseUrl + "/urls/2").asString();
            assertThat(response.getStatus()).isEqualTo(404);
        }
    }


}
