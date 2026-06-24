package org.ariake.examples.staticcontent;

import io.helidon.http.HeaderNames;
import io.helidon.http.Method;
import io.helidon.http.PathMatchers;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.staticcontent.FileSystemHandlerConfig;
import io.helidon.webserver.staticcontent.StaticContentFeature;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.ariake.server.helidon.HelidonRoutingService;
import sting.Injectable;

@Injectable
final class StaticContentRoutes implements HelidonRoutingService {
    private static final String STATIC_CONTEXT = "/static";
    private static final String BROTLI_EXTENSION = ".br";
    private static final String BROTLI_ENCODING = "br";
    private static final int YEAR_IN_SECONDS = 365 * 24 * 60 * 60;

    private final StaticContentRoot root;

    StaticContentRoutes(final StaticContentRoot root) {
        this.root = root;
    }

    @Override
    public void routing(final HttpRouting.Builder routing) {
        routing.register(STATIC_CONTEXT, rules -> {
            rules.route(Method.predicate(Method.GET, Method.HEAD), PathMatchers.any(), this::cacheControl);
            rules.route(Method.predicate(Method.GET, Method.HEAD), PathMatchers.any(), this::preEncodedBrotli);
            rules.register(StaticContentFeature.createService(FileSystemHandlerConfig.builder()
                    .location(root.path())
                    .welcome("index.nocache.html")
                    .build()));
        });
    }

    private void cacheControl(final ServerRequest request, final ServerResponse response) {
        final String requestPath = staticRequestPath(request);
        if (requestPath.contains(".nocache.")) {
            final Instant now = Instant.now();
            response.header("Date", formatHttpDate(now));
            response.header("Last-Modified", formatHttpDate(now));
            response.header("Expires", formatHttpDate(Instant.EPOCH));
            response.header("Pragma", "no-cache");
            response.header("Cache-Control", "no-cache, must-revalidate, pre-check=0, post-check=0");
        } else if (requestPath.contains(".cache.")) {
            response.header("Expires", formatHttpDate(Instant.now().plus(Duration.ofDays(365))));
            response.header("Cache-Control", "max-age=" + YEAR_IN_SECONDS + ", public, immutable");
            response.header("Pragma", "");
        }
        response.next();
    }

    private void preEncodedBrotli(final ServerRequest request, final ServerResponse response) throws Exception {
        final String requestPath = staticRequestPath(request);
        final Path file = root.resolveRequestPath(requestPath);
        if (!acceptsBrotli(request)
                || requestPath.endsWith(BROTLI_EXTENSION)
                || null == file
                || !Files.isRegularFile(file)) {
            response.next();
            return;
        }

        final var encodedFile = Path.of( file + BROTLI_EXTENSION);
        if (!Files.isRegularFile(encodedFile)) {
            response.next();
            return;
        }

        response.header("Content-Encoding", BROTLI_ENCODING);
        response.header("Content-Length", Long.toString(Files.size(encodedFile)));
        response.header("Content-Type", contentType(requestPath));
        try (var in = Files.newInputStream(encodedFile);
                var out = response.outputStream()) {
            in.transferTo(out);
        }
    }

    private static boolean acceptsBrotli(final ServerRequest request) {
        return request.headers()
                .first(HeaderNames.create("Accept-Encoding"))
                .orElse("")
                .contains(BROTLI_ENCODING);
    }

    private static String staticRequestPath(final ServerRequest request) {
        final String path = request.path().rawPathNoParams();
        if (path.isEmpty() || "/".equals(path) || STATIC_CONTEXT.equals(path)) {
            return STATIC_CONTEXT;
        } else if (path.startsWith(STATIC_CONTEXT + "/")) {
            return path;
        } else if (path.startsWith("/")) {
            return STATIC_CONTEXT + path;
        } else if (path.equals(STATIC_CONTEXT.substring(1)) || path.startsWith(STATIC_CONTEXT.substring(1) + "/")) {
            return "/" + path;
        } else {
            return STATIC_CONTEXT + "/" + path;
        }
    }

    private static String formatHttpDate(final Instant value) {
        return DateTimeFormatter.RFC_1123_DATE_TIME.format(value.atZone(ZoneOffset.UTC));
    }

    static String contentType(final String path) {
        final String pathWithoutEncoding =
                path.endsWith(BROTLI_EXTENSION) ? path.substring(0, path.length() - BROTLI_EXTENSION.length()) : path;
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
