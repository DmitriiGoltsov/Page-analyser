package hexlet.code.controllers;

import hexlet.code.URL;
import hexlet.code.query.QURL;
import io.javalin.http.Handler;

public class URLController {

    public static Handler createURL = ctx -> {
        String urlName = ctx.formParam("name");

        URL urlToAdd = new URL(urlName);

        if (urlName.isEmpty() || urlName == null) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.attribute("url", urlToAdd);
            ctx.render("templates/index.html");
            return;
        }

        URL urlToCheck = new QURL()
                .name.iequalTo(urlName)
                .findOne();

        if (urlToAdd.getName().equals(urlToCheck.getName())) {
            ctx.sessionAttribute("flash", "Страница уже существует");
            ctx.sessionAttribute("flash-type", "info");
            ctx.redirect("/urls");
            return;
        }

        urlToAdd.save();

        ctx.sessionAttribute("flash", "Страница успешно добавлена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls");

    };
}
