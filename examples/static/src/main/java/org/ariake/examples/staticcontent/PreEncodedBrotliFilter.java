package org.ariake.examples.staticcontent;

import java.nio.file.Files;
import java.nio.file.Path;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpFilter;
import org.ariake.http.HttpFilterChain;

final class PreEncodedBrotliFilter implements HttpFilter {
    private static final String EXTENSION = ".br";
    private static final String ENCODING = "br";

    private final StaticContentRoot root;

    PreEncodedBrotliFilter(final StaticContentRoot root) {
        this.root = root;
    }

    @Override
    public void filter(final HttpExchange exchange, final HttpFilterChain chain) throws Exception {
        final String acceptEncoding = exchange.requestHeader("Accept-Encoding").orElse("");
        final String resourcePath = exchange.path();
        final Path file = root.resolveRequestPath(resourcePath);
        if (!acceptEncoding.contains(ENCODING)
                || resourcePath.endsWith(EXTENSION)
                || null == file
                || !Files.isRegularFile(file)) {
            chain.proceed(exchange);
            return;
        }

        final Path encodedFile = Path.of(file.toString() + EXTENSION);
        if (!Files.isRegularFile(encodedFile)) {
            chain.proceed(exchange);
            return;
        }

        exchange.header("Content-Encoding", ENCODING);
        exchange.header("Content-Type", StaticContentService.contentType(resourcePath));
        chain.proceed(new PathOverrideHttpExchange(exchange, resourcePath + EXTENSION));
    }
}
