package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import lombok.extern.slf4j.Slf4j;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Slf4j
public class UrlController {

    public static Handler createUrl = ctx -> {

        String urlName = ctx.formParam("url");
        log.debug("urlName is: " + urlName);

        URL parsedUrl;
        try {
            parsedUrl = new URL(urlName);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            log.debug("An exception has occurred: " + e.getMessage());
            return;
        }

        String protocol = parsedUrl.getProtocol();
        String host = parsedUrl.getHost();
        int port = parsedUrl.getPort();
        String portAsString =
                port == -1
                ? ""
                : ":" + port;

        String urlAddress = String.format(
                "%s://%s%s",
                protocol,
                host,
                portAsString
        );

        Url existingUrl = UrlRepository.findByName(urlAddress).orElse(null);

        if (existingUrl != null) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "warning");

            log.info("URL already exists!");
        } else {
            Url urlToSave = new Url(urlAddress);
            UrlRepository.save(urlToSave);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");

            log.info("URL ADDED SUCCESSFULLY");
        }

        ctx.redirect("/urls");
    };

    public static Handler showUrls = ctx -> {
        log.debug("Попытка загрузить URLs");

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        List<Url> urls = UrlRepository.getUrls();

        Map<Long, UrlCheck> urlChecks = null;
        try {
            urlChecks = UrlCheckRepository.findLatestChecks();
        } catch (SQLException throwables) {
            log.error(throwables.getMessage(), throwables);
        }

        log.debug("urls is: " + urls);
        log.debug("urlChecks is: " + urlChecks);

        int lastPage = urls.size() + 1;
        int currentPage = page + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();

        ctx.attribute("urls", urls);
        ctx.attribute("urlChecks", urlChecks);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.render("urls/showURLs.html");

        log.info("URLS PAGE IS RENDERED");
    };

    public static Handler showUrlById = ctx -> {
        log.info("Trying to find URL by its id");

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlRepository.findById(id).orElse(null);

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        List<UrlCheck> checks = UrlCheckRepository.getAllChecks(url.getId());

        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.render("urls/show.html");
    };
}
