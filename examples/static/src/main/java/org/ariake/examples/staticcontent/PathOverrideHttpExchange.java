package org.ariake.examples.staticcontent;

import java.time.Instant;
import java.util.Optional;
import org.ariake.http.HttpExchange;
import org.ariake.http.HttpMethod;

final class PathOverrideHttpExchange implements HttpExchange {
    private final HttpExchange delegate;
    private final String path;

    PathOverrideHttpExchange(final HttpExchange delegate, final String path) {
        this.delegate = delegate;
        this.path = path;
    }

    @Override
    public HttpMethod method() {
        return delegate.method();
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String requestUri() {
        return delegate.requestUri();
    }

    @Override
    public Optional<String> requestHeader(final String name) {
        return delegate.requestHeader(name);
    }

    @Override
    public String body() {
        return delegate.body();
    }

    @Override
    public void status(final int status) {
        delegate.status(status);
    }

    @Override
    public void header(final String name, final String value) {
        delegate.header(name, value);
    }

    @Override
    public void dateHeader(final String name, final Instant value) {
        delegate.dateHeader(name, value);
    }

    @Override
    public void send() {
        delegate.send();
    }

    @Override
    public void send(final String body) {
        delegate.send(body);
    }

    @Override
    public void send(final byte[] body, final String contentType) {
        delegate.send(body, contentType);
    }

    @Override
    public void sendError(final int status, final String message) {
        delegate.sendError(status, message);
    }

    @Override
    public boolean isSent() {
        return delegate.isSent();
    }
}
