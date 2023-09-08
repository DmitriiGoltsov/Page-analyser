package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class UrlController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class.getName());

    public static Handler createURL = ctx -> {

        String urlName = ctx.formParam("url");
        LOGGER.debug("urlName is: " + urlName);

        URL parsedUrl;
        try {
            parsedUrl = new URL(urlName);
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            LOGGER.debug("An exception has occurred: " + e.getMessage());
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

            LOGGER.info("URL already exists!");
        } else {
            Url urlToSave = new Url(urlAddress, Timestamp.from(Instant.now()));
            UrlRepository.save(urlToSave);

            ctx.sessionAttribute("flash", "Страница успешно добавлена");
            ctx.sessionAttribute("flash-type", "success");

            LOGGER.info("URL ADDED SUCCESSFULLY");
        }

        ctx.redirect("/urls");
    };

    public static Handler showURLs = ctx -> {
        LOGGER.debug("Попытка загрузить URLs");

        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        List<Url> urls = UrlRepository.getUrls();

        Map<Long, UrlCheck> urlChecks = new HashMap<>();
        List<UrlCheck> checks = new ArrayList<>();

        for (Url url : urls) {
            urlChecks.put(url.getId(), UrlCheckRepository.findLastCheckByUrlId(url.getId()).orElse(null));
            UrlCheck lastCheck = UrlCheckRepository.findLastCheckByUrlId(url.getId()).orElse(null);

            if (lastCheck != null) {
                checks.add(lastCheck);
            }
        }

        LOGGER.info("urls is: " + urls);
        LOGGER.info("urlChecks is: " + urlChecks);
        LOGGER.info("checks is: " + checks);

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

        LOGGER.info("URLS PAGE IS RENDERED");
    };

    public static Handler showURLById = ctx -> {
        LOGGER.info("Trying to find URL by its id");

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = UrlRepository.findById(id).orElse(null);

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        Instant urlCreationTime = url.getCreatedAt().toInstant();

        List<UrlCheck> checks = UrlCheckRepository.getAllChecks(url.getId());

        Map<UrlCheck, Instant> creationTimeMap = new HashMap<>();

        for (UrlCheck check : checks) {
            creationTimeMap.put(check, check.getCreatedAt());
        }

        ctx.attribute("url", url);
        ctx.attribute("checks", checks);
        ctx.attribute("urlCreationTime", urlCreationTime);
        ctx.attribute("creationTimeMap", creationTimeMap);
        ctx.render("urls/show.html");
    };
}
