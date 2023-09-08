package hexlet.code.controllers;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.repository.UrlCheckRepository;
import hexlet.code.repository.UrlRepository;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Timestamp;

public class UrlCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UrlController.class.getName());

    public static Handler addCheck = ctx -> {
        LOGGER.debug("addCheck Handler: trying to save an UrlCheck entity to DB");

        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        LOGGER.info("Url's id is " + id);

        Url url = UrlRepository.findById(id).orElse(null);

        LOGGER.info("Url is" + url);

        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            int statusCode = response.getStatus();

            Document document = Jsoup.parse(response.getBody());
            String title = document.title();
            Element h1Element = document.selectFirst("h1");
            String h1 = h1Element == null
                    ? ""
                    : h1Element.text();
            Element descriptionElement = document.selectFirst("meta[name=description]");
            String description = descriptionElement == null
                    ? ""
                    : descriptionElement.attr("content");
            Timestamp createdAt = new Timestamp(System.currentTimeMillis());

            LOGGER.info("Trying to create a new object of UrlCheck class");

            UrlCheck urlCheckToAdd = new UrlCheck(statusCode, title, h1, description, createdAt, url.getId());

            LOGGER.info("UrlCheckToAdd's fields are these: statusCode " + statusCode + " title " + title + " h1 " + h1
                    + " description " + description + " createdAt " + createdAt + " urlId " + urlCheckToAdd.getUrlId());

            UrlCheckRepository.save(urlCheckToAdd);

            ctx.sessionAttribute("flash", "Страница успешно проверена");
            ctx.sessionAttribute("flash-type", "success");
            LOGGER.info("Check is done and added to the DB");
        } catch (UnirestException e) {
            ctx.sessionAttribute("flash", "Некорректный адрес");
            ctx.sessionAttribute("flash-type", "danger");
        } catch (Exception e) {
            ctx.sessionAttribute("flash", e.getMessage());
            ctx.sessionAttribute("flash-type", "danger");
        }

        ctx.redirect("/urls/" + url.getId());
    };
}
