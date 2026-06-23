package org.ariake.examples.staticcontent;

import java.time.Duration;
import java.time.Instant;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpFilter;
import org.ariake.http.HttpFilterChain;

final class CacheControlFilter implements HttpFilter {
    private static final int YEAR_IN_SECONDS = 365 * 24 * 60 * 60;

    @Override
    public void filter(final HttpExchange exchange, final HttpFilterChain chain) throws Exception {
        final String requestUri = exchange.requestUri();

        if (requestUri.contains(".nocache.")) {
            final Instant now = Instant.now();
            exchange.dateHeader("Date", now);
            exchange.dateHeader("Last-Modified", now);
            exchange.dateHeader("Expires", Instant.EPOCH);
            exchange.header("Pragma", "no-cache");
            exchange.header("Cache-control", "no-cache, must-revalidate, pre-check=0, post-check=0");
        } else if (requestUri.contains(".cache.")) {
            exchange.dateHeader("Expires", Instant.now().plus(Duration.ofDays(365)));
            exchange.header("Cache-control", "max-age=" + YEAR_IN_SECONDS + ", public, immutable");
            exchange.header("Pragma", "");
        }

        chain.proceed(exchange);
    }
}
