package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class URLController {

    private static final int ROWS_PER_PAGES = 10;

    public static Handler createURL = ctx -> {

        String urlName = ctx.formParam("url");

        System.out.println("urlName is: " + urlName);

        URL rawURL;
        try {
            rawURL = new URL(urlName);
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("templates/index.html");
            return;
        }

        String protocol = rawURL.getProtocol();
        String host = rawURL.getHost();
        int port = rawURL.getPort();

        String normalizedUrl = getNormalizedUrl(protocol, host, port);

        System.out.println("normalizedUrl is: " + normalizedUrl);

        if (doesUrlAlreadyExist(normalizedUrl)) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        Url urlToAdd = new Url(normalizedUrl);
        urlToAdd.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showURLs = ctx -> {
        Url url = new QUrl()
                .name.equalTo("ya.ru")
                .findOne();

        if (url == null) {
            Url url1 = new Url("ya.ru");
            url1.save();
        }


        Url urlDb = new QUrl()
                .name.equalTo("ya.ru")
                .findOne();
        System.out.println("read url from DB: " + urlDb);

        List<Url> urls = new ArrayList<>();
        urls.add(urlDb);

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    public static Handler showURLById = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");

    };

    @NotNull
    private static String getNormalizedUrl(String protocol, String host, int port) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol).append("://").append(host);

        if (port != -1) {
            sb.append(port);
        }

        String normalizedUrl = sb.toString();
        return normalizedUrl;
    }

    private static boolean doesUrlAlreadyExist(String urlToCheck) {
        Url urlFromDB = new QUrl()
                .name.iequalTo(urlToCheck)
                .findOne();

        return urlFromDB != null;
    }
}
