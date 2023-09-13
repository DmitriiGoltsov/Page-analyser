package hexlet.code;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import hexlet.code.controllers.UrlCheckController;
import hexlet.code.repository.BaseRepository;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import lombok.extern.slf4j.Slf4j;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;

@Slf4j
public class App {

    private static final String DEFAULT_PORT = "8085";
    private static final String DEFAULT_MODE = "production";
    private static final String ADDITIONAL_MODE = "development";

    public static void main(String[] args) throws SQLException, IOException {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static boolean isProduction() {
        return getMode().equals(DEFAULT_MODE);
    }

    public static Javalin getApp() throws IOException, SQLException {

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(getDatabaseUrl());

        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        var url = App.class.getClassLoader().getResource("schema.sql");

        File file;
        String sql;

        try {
            file = new File(url.getFile());
            sql = Files.lines(file.toPath())
                    .collect(Collectors.joining("\n"));
        } catch (NoSuchFileException e) {
            sql = """
                    DROP TABLE IF EXISTS urls;
                    create table urls
                    (
                        id         bigint generated by default as identity not null,
                        name       varchar(255),
                        created_at timestamp                               not null
                    );
                    DROP TABLE IF EXISTS url_checks;
                    create table url_checks
                    (
                        id          bigint generated by default as identity not null,
                        status_code integer                                 not null,
                        title       varchar(255),
                        h1          varchar(255),
                        description text,
                        created_at  timestamp                               not null,
                        url_id      bigint                                  not null
                    );
                    """;
    }


        try (Connection connection = hikariDataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        BaseRepository.dataSource = hikariDataSource;

        Javalin app = Javalin.create(config -> {
            if (!isProduction()) {
                config.plugins.enableDevLogging();
            }

            JavalinThymeleaf.init(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    private static void addRoutes(Javalin app) {

        app.get("/", RootController.welcome);

        app.routes(() -> {
            path("/urls", () -> {
                get(UrlController.showUrls);
                post(UrlController.createUrl);
                path("/{id}", () -> {
                    get(UrlController.showUrlById);
                    path("/checks", () -> {
                        post(UrlCheckController.addCheck);
                    });
                });
            });
        });
    }

    private static String getMode() {
        return System.getenv()
                .getOrDefault("APP_ENV", ADDITIONAL_MODE);
    }

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", DEFAULT_PORT);
        return Integer.valueOf(port);
    }
    private static String getDatabaseUrl() {
        return System.getenv()
                .getOrDefault("JDBC_DATABASE_URL", "jdbc:h2:mem:project");
    }

    private static TemplateEngine getTemplateEngine() {

        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCharacterEncoding("UTF-8");

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

}
