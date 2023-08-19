package hexlet.code;

import hexlet.code.controllers.RootController;
import hexlet.code.controllers.UrlController;
import hexlet.code.controllers.UrlCheckController;
import io.javalin.Javalin;
import io.javalin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;

public class App {

    private static final String DEFAULT_PORT = "8085";
    private static final String DEFAULT_MODE = "production";
    private static final String ADDITIONAL_MODE = "development";

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }

    private static boolean isProduction() {
        return getMode().equals(DEFAULT_MODE);
    }

    public static Javalin getApp() {

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
                get(UrlController.showURLs);
                post(UrlController.createURL);
                path("/{id}", () -> {
                    get(UrlController.showURLById);
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
