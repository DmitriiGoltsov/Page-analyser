package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class UrlCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLController.class.getName());

    public static Handler checkUrl = ctx -> {
        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        LOGGER.debug("Поиск URL по id {}", id);

        Url url = new QUrl()
                .id.equalTo(id)
                .findOne();

        if (Objects.isNull(url)) {
            throw new NotFoundResponse(String.format("Url with id=%d is not found", id));
        }

        LOGGER.debug("Попытка провести проверку URL {}", url.getName());
        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();
            String content = response.getBody();

            Document body = Jsoup.parse(content);
            int statusCode = response.getStatus();
            String title = body.title();

            String h1 = body.selectFirst("h1") != null
                    ? Objects.requireNonNull(body.selectFirst("h1")).text()
                    : "";

            String description = body.selectFirst("meta[name=description]") != null
                    ? Objects.requireNonNull(body.selectFirst("meta[name=description]")).attr("content")
                    : "";

            UrlCheck checkedUrl = new UrlCheck(statusCode, title, h1, description, url);
            checkedUrl.save();

            LOGGER.debug("URL {} был проверен", url);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");

        } catch (UnirestException e) {
            LOGGER.debug("Не удалось проверить URL {}", url.getName());
            ctx.sessionAttribute("flash", "Не удалось проверить страницу");
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + id);
    };
}
