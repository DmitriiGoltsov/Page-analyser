package hexlet.code.controllers;

import hexlet.code.URLEntity;
import hexlet.code.query.QURLEntity;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.stream.IntStream;

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

        URLEntity urlToAdd = new URLEntity(normalizedUrl);
        urlToAdd.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showURLs = ctx -> {

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        PagedList<URLEntity> pagedURLs = new QURLEntity()
                .setFirstRow(page * ROWS_PER_PAGES)
                .setMaxRows(ROWS_PER_PAGES)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<URLEntity> URLs = pagedURLs.getList();

        int currentPage = pagedURLs.getPageIndex() + 1;
        int lastPage = pagedURLs.getTotalPageCount() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("urls", URLs);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);

        ctx.render("urls/index.html");
    };

    public static Handler showURLById = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        URLEntity url = new QURLEntity()
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
        URLEntity urlFromDB = new QURLEntity()
                .name.iequalTo(urlToCheck)
                .findOne();

        return urlFromDB != null;
    }
}
