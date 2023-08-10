package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

public class URLController {

    private static final int ROWS_PER_PAGES = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(URLController.class.getName());

    public static Handler createURL = ctx -> {

        String urlName = ctx.formParam("url");
        LOGGER.info("urlName is: " + urlName);

        URL rawURL;
        try {
            LOGGER.info("Пытаюсь создать новый урл {}", urlName);
            rawURL = new URL(Objects.requireNonNull(urlName));
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("templates/index.html");
            LOGGER.debug("\n" + "An exception has occurred" + "\n");
            return;
        }

        LOGGER.info("\n" + "Trying to normalize url" + "\n");
        String protocol = rawURL.getProtocol();
        String host = rawURL.getHost();
        int port = rawURL.getPort();

        String normalizedUrl = getNormalizedUrl(protocol, host, port);
        LOGGER.info("\n" + "normalizedUrl is: " + normalizedUrl + "\n");
        LOGGER.info("\n" + doesUrlAlreadyExist(normalizedUrl) + "\n");

        if (doesUrlAlreadyExist(normalizedUrl)) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            LOGGER.info("\n" + "URL already exists" + "\n");
            return;
        }

        Url urlToAdd = new Url(normalizedUrl);
        urlToAdd.save();
        LOGGER.info("\n" + "Page has been successfully added." + "\n");

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");
    };

    public static Handler showURLs = ctx -> {
        LOGGER.info("Попытка загрузить URLs");
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
        int offset = page * ROWS_PER_PAGES - ROWS_PER_PAGES;

        PagedList<Url> pagedUrl = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(ROWS_PER_PAGES)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrl.getList();
        int currentPage = pagedUrl.getPageIndex() + 1;
        int lastPage = pagedUrl.getTotalPageCount() + 1;

        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.attribute("urls", urls);

        ctx.render("urls/showURLs.html");
        LOGGER.info("\n" + "Urls выведены на экран" + "\n");
    };

    public static Handler showURLById = ctx -> {
        LOGGER.info("Trying to find URL by its id");
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
