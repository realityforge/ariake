package org.ariake.examples.staticcontent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.ariake.http.AriakeHttpService;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpRoutes;
import sting.Injectable;

@Injectable
public final class StaticContentService implements AriakeHttpService {
    private final StaticContentRoot root;
    private final PageViewService pageViews;

    StaticContentService(final StaticContentRoot root, final PageViewService pageViews) {
        this.root = root;
        this.pageViews = pageViews;
    }

    @Override
    public void routes(final HttpRoutes routes) {
        routes.filter(0, "/static/*", new CacheControlFilter());
        routes.filter(10, "/static/*", new PreEncodedBrotliFilter(root));
        routes.get("/static", this::serve);
        routes.get("/static/{+path}", this::serve);
    }

    private void serve(final HttpExchange exchange) throws IOException {
        final Path file = root.resolveRequestPath(exchange.path());
        if (null == file || !Files.isRegularFile(file)) {
            exchange.sendError(404, "Not Found");
            return;
        }

        pageViews.record(servedPath(exchange.path()));
        exchange.send(Files.readAllBytes(file), contentType(exchange.path()));
    }

    private static String servedPath(final String path) {
        return path.endsWith(".br") ? path.substring(0, path.length() - 3) : path;
    }

    static String contentType(final String path) {
        final String pathWithoutEncoding = path.endsWith(".br") ? path.substring(0, path.length() - 3) : path;
        if (pathWithoutEncoding.endsWith(".html")) {
            return "text/html; charset=utf-8";
        } else if (pathWithoutEncoding.endsWith(".css")) {
            return "text/css; charset=utf-8";
        } else if (pathWithoutEncoding.endsWith(".js")) {
            return "text/javascript; charset=utf-8";
        } else if (pathWithoutEncoding.endsWith(".json")) {
            return "application/json; charset=utf-8";
        } else if (pathWithoutEncoding.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (pathWithoutEncoding.endsWith(".txt")) {
            return "text/plain; charset=utf-8";
        } else {
            return "application/octet-stream";
        }
    }
}
