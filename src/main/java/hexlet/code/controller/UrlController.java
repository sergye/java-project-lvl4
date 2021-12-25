package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.query.QUrl;
import io.javalin.http.Handler;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class UrlController {
    public static Handler getUrls = ctx -> {

        List<Url> urls = new QUrl().orderBy().id.asc().findList();

        ctx.attribute("urls", urls);
        ctx.render("urls/index.html");
    };

    public static Handler getUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl().id.equalTo(id).findOne();

        ctx.attribute("url", url);
        ctx.render("urls/show.html");
    };

    public static Handler addUrl = ctx -> {
        try {

            URL urlFromParam = new URL(ctx.formParam("url"));
            String url = urlFromParam.getProtocol() + "://" + urlFromParam.getAuthority();

            boolean urlExists = new QUrl().name.equalTo(url).exists();

            if (urlExists) {
                ctx.sessionAttribute("flash", "Страница уже существует");
                ctx.sessionAttribute("flash-type", "info");
            } else {
                new Url(url).save();

                ctx.sessionAttribute("flash", "Страница успешно добавлена");
                ctx.sessionAttribute("flash-type", "success");
            }
        } catch (MalformedURLException e) {
            ctx.sessionAttribute("flash", "Некорректный URL");
            ctx.sessionAttribute("flash-type", "danger");
            ctx.redirect("/");
            return;
        }
        ctx.redirect("/urls");
    };
}
