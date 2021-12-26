package hexlet.code.controller;

import hexlet.code.model.Url;
import hexlet.code.model.UrlCheck;
import hexlet.code.model.query.QUrl;
import io.javalin.http.Handler;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

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

    public static Handler checkUrl = ctx -> {
        long id = ctx.pathParamAsClass("id", Long.class).getOrDefault(null);

        Url url = new QUrl().id.equalTo(id).findOne();

        HttpResponse<String> response = Unirest.get(url.getName()).asString();

        int statusCode = response.getStatus();
        Document body = Jsoup.parse(response.getBody());
        String title = body.title();
        String description = null;
        String h1 = null;

        if (body.selectFirst("meta[name=description]") != null) {
            description = body.selectFirst("meta[name=description]").attr("content");
        }


        if (body.selectFirst("h1") != null) {
            h1 = body.selectFirst("h1").text();
        }


        UrlCheck urlCheck = new UrlCheck(statusCode, title, h1, description, url);
        urlCheck.save();

        ctx.sessionAttribute("flash", "Страница успешно проверена");
        ctx.sessionAttribute("flash-type", "success");
        ctx.redirect("/urls/" + id);
    };
}
