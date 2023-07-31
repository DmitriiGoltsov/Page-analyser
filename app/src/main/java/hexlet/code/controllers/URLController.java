package hexlet.code.controllers;

import hexlet.code.URL;
import hexlet.code.query.QURL;
import io.ebean.PagedList;
import io.javalin.http.Handler;
import io.javalin.http.NotFoundResponse;
import org.objectweb.asm.Handle;

import java.util.List;
import java.util.stream.IntStream;

public class URLController {

    private static final int ROWS_PER_PAGES = 10;

    public static Handler createURL = ctx -> {
        String urlName = ctx.formParam("url");

        System.out.println("urlName is: " + urlName);

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

        if (urlToCheck != null) {
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

    public static Handler showURLs = ctx -> {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1) - 1;

        PagedList<URL> pagedURLs = new QURL()
                .setFirstRow(page * ROWS_PER_PAGES)
                .setMaxRows(ROWS_PER_PAGES)
                .orderBy()
                    .id.asc()
                .findPagedList();

        List<URL> URLs = pagedURLs.getList();

        int currentPage = pagedURLs.getPageIndex() + 1;
        int lastPage = pagedURLs.getTotalPageCount() + 1;
        List<Integer> pages = IntStream
                .range(1, lastPage)
                .boxed()
                .toList();


        ctx.attribute("urls", URLs);
        ctx.attribute("pages", pages);
        ctx.attribute("currentPage", currentPage);
        ctx.redirect("/urls/index.html");
    };

    public static Handler showURL = ctx -> {
        int id = ctx.pathParamAsClass("id", Integer.class).getOrDefault(null);

        URL url = new QURL()
                .id.equalTo(id)
                .findOne();

        if (url == null) {
            throw new NotFoundResponse("The ulr you are looking for is not found");
        }

        ctx.attribute("url", url);
        ctx.render("urls/show.html");

    };
}
