package hexlet.code;

import hexlet.code.controller.RootController;
import hexlet.code.controller.UrlController;
import io.javalin.Javalin;
import io.javalin.plugin.rendering.template.JavalinThymeleaf;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;


public final class App {

    private static final String PORT = "PORT";
    private static final String PORT_NUMBER = "3000";
    private static final String APP_ENV = "APP_ENV";
    private static final String DEVELOPMENT_MODE = "development";
    private static final String PREFIX = "/templates/";

    public static void main(String[] args) {
        Javalin app = getApp();
        String port = System.getenv().getOrDefault(PORT, PORT_NUMBER);
        app.start(Integer.parseInt(port));
    }

    public static Javalin getApp() {
        Javalin app = Javalin.create(config -> {
            if (isDevelopment()) {
                config.enableDevLogging();
            }
            config.enableWebjars();
            JavalinThymeleaf.configure(getTemplateEngine());
        });

        addRoutes(app);

        app.before(ctx -> {
            ctx.attribute("ctx", ctx);
        });

        return app;
    }

    private static boolean isDevelopment() {
        String mode = System.getenv().getOrDefault(APP_ENV, DEVELOPMENT_MODE);
        return mode.equals(DEVELOPMENT_MODE);
    }

    private static TemplateEngine getTemplateEngine() {
        TemplateEngine templateEngine = new TemplateEngine();

        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix(PREFIX);

        templateEngine.addTemplateResolver(templateResolver);
        templateEngine.addDialect(new LayoutDialect());
        templateEngine.addDialect(new Java8TimeDialect());

        return templateEngine;
    }

    private static void addRoutes(Javalin app) {
        app.get("/", RootController.welcome);

        app.routes(() -> {
            path("urls", () -> {
                get(UrlController.getUrls);
                post(UrlController.addUrl);
                get("{id}", UrlController.getUrl);
                post("{id}/checks", UrlController.checkUrl);
            });
        });
    }
}
