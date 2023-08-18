package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.List;
import java.util.stream.IntStream;

public class URLController {

    private static final int ROWS_PER_PAGES = 10;
    private static final Logger LOGGER = LoggerFactory.getLogger(URLController.class.getName());

    public static Handler createURL = ctx -> {

        String urlName = ctx.formParam("url");
        LOGGER.info("urlName is: " + urlName);
        String urlAddress;

        try {
            URL rawUrl = new URL(urlName);
            String protocol = rawUrl.getProtocol();
            String host = rawUrl.getHost();
            int port = rawUrl.getPort();
            String portAsString = port == -1
                    ? ""
                    : String.valueOf(port);

            urlAddress = protocol + "://" + host + portAsString;
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/index.html");
            LOGGER.debug("An exception has occurred: " + e.getMessage());
            return;
        }

        Url existingUrl = new QUrl()
                .name.ieq(urlAddress)
                .findOne();

        if (existingUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "success");

            ctx.redirect("/urls");
            LOGGER.info("URL already exists!");
            return;
        }

        Url urlToSave = new Url(urlAddress);
        urlToSave.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");

        ctx.redirect("/urls");
        LOGGER.info("URL ADDED SUCCESSFULLY");
    };

    public static Handler showURLs = ctx -> {
        LOGGER.info("Попытка загрузить URLs");

        int normalizedPage;
        try {
            String page = ctx.queryParam("page");
            normalizedPage = Integer.parseInt(page);
        } catch (IllegalArgumentException e) {
            normalizedPage = 1;
        }

        int offset = (normalizedPage - 1) * ROWS_PER_PAGES;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(offset)
                .setMaxRows(ROWS_PER_PAGES)
                .orderBy()
                .id.asc()
                .findPagedList();

        int pagesCount = pagedUrls.getTotalPageCount();

        int[] pages = IntStream.rangeClosed(1, pagesCount).toArray();
        List<Url> urls = pagedUrls.getList();

        ctx.attribute("urls", urls);
        ctx.attribute("currentPage", normalizedPage);
        ctx.attribute("pages", pages);

        ctx.render("urls/showURLs");
        LOGGER.info("URLS PAGE IS RENDERED");
    };

    public static Handler showURLById = ctx -> {
        LOGGER.info("Trying to find URL by its id");
        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        List<UrlCheck> checks = url.getUrlChecks();

        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.render("urls/show.html");
        LOGGER.info("Page of " + url.getName() + "is being rendered");
    };
}
