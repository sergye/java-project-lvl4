package hexlet.code;

import io.javalin.Javalin;
import io.javalin.core.JavalinConfig;

public final class App {

    private static int getPort() {
        String port = System.getenv().getOrDefault("PORT", "5000");
        return Integer.valueOf(port);
    }

    public static Javalin getApp() {
        final Javalin app = Javalin.create(config -> {
            config.enableDevLogging();
        });

        app.get("/", ctx -> ctx.result("Hello World!"));

        return app;
    }

    public static void main(String[] args) {
        Javalin app = getApp();
        app.start(getPort());
    }
}