package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import hexlet.code.model.query.QUrlCheck;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class UrlController {

    private static final int ROWS_PER_PAGES = 12;
    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class.getName());

    public static Handler createURL = ctx -> {

        String urlName = ctx.formParam("url");
        LOGGER.info("urlName is: " + urlName);

        String urlAddress;
        URL parcedUrl;
        try {
            parcedUrl = new URL(urlName);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            LOGGER.debug("An exception has occurred: " + e.getMessage());
            return;
        }

        String protocol = parcedUrl.getProtocol();
        String host = parcedUrl.getHost();
        int port = parcedUrl.getPort();
        String portAsString = port == -1
                ? ""
                : ":" + port;

        urlAddress = String.format(
                "%s://%s%s",
                protocol,
                host,
                portAsString
        );

        Url existingUrl = new QUrl()
                .name.ieq(urlAddress)
                .findOne();

        if (existingUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "success");

            ctx.redirect("/");
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

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        PagedList<Url> pagedUrls = new QUrl()
                .setFirstRow(page * ROWS_PER_PAGES)
                .setMaxRows(ROWS_PER_PAGES)
                .orderBy()
                .id.asc()
                .findPagedList();

        List<Url> urls = pagedUrls.getList();

        Map<Long, UrlCheck> urlChecks = new QUrlCheck()
                .url.id.asMapKey()
                .orderBy()
                .createdAt.desc()
                .findMap();

        int lastPage = pagedUrls.getTotalPageCount() + 1;
        int currentPage = pagedUrls.getPageIndex() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/showURLs.html");

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
