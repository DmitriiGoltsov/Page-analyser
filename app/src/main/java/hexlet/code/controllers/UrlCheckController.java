package hexlet.code.controllers;

import groovy.util.logging.Log;
import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.ebean.DB;
import io.ebean.Transaction;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;
import org.h2.schema.Domain;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Objects;

public class UrlCheckController {

    private static final Logger LOGGER = LoggerFactory.getLogger(URLController.class.getName());

    public static Handler addCheck = ctx -> {
        Long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);
        String body = "";
        int statusCode = 0;

        Url url = new QUrl()
                .id.equalTo(id)
                .forUpdate()
                .findOne();

        try {
            HttpResponse<String> response = Unirest
                    .get(url.getName())
                    .asString();

            body = response.getBody();
            statusCode = response.getStatus();
        } catch (Exception e) {
            ctx.sessionAttribute("flash", "Неверный адрес");
            ctx.sessionAttribute("flash-type", "danger");

            ctx.attribute("url", url);
            ctx.attribute("checks", url.getUrlChecks());
            LOGGER.error(url.getName() + " is not correct address!");
            ctx.redirect("/urls/" + id);
        }

        Document document = Jsoup.parse(body);
        String title = getTagValue(document, "title");
        String h1 = getTagValue(document, "h1");
        String description = getDescription(document);

        UrlCheck checkToAdd = new UrlCheck(statusCode, title, h1, description, url);

        try (Transaction transaction = DB.beginTransaction()){
            url.addUrlCheck(checkToAdd);
            checkToAdd.save();
            url.save();

            transaction.commit();
        }

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");

        ctx.attribute("url", url);
        ctx.attribute("checks", url.getUrlChecks());
        ctx.redirect("/urls/" + id);
        LOGGER.info("Check is done and added to the DB");
    };

    private static String getTagValue (Document document, String tag) {
        Element element = document.selectFirst(tag);

        return element != null
                ? element.text()
                : "";
    }

    private static String getDescription(Document document) {
        Element element = document.selectFirst("meta[name=description]");

        return element != null
                ? element.attr("content")
                : "";
    }
}
