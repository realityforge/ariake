package org.ariake.http;

import java.util.List;
import java.util.Objects;

public record HttpFilterRegistration(int order, List<String> pathPatterns, HttpFilter filter) {
    public HttpFilterRegistration {
        pathPatterns = List.copyOf(pathPatterns);
        Objects.requireNonNull(filter);
        if (pathPatterns.isEmpty()) {
            throw new IllegalArgumentException("HTTP filter must declare at least one path pattern");
        }
        for (String pathPattern : pathPatterns) {
            validatePathPattern(pathPattern);
        }
    }

    public boolean matches(final String path) {
        Objects.requireNonNull(path);
        for (String pathPattern : pathPatterns) {
            if (matchesPattern(pathPattern, path)) {
                return true;
            }
        }
        return false;
    }

    private static void validatePathPattern(final String pathPattern) {
        Objects.requireNonNull(pathPattern);
        if (!pathPattern.startsWith("/")) {
            throw new IllegalArgumentException("HTTP filter path pattern must start with '/': " + pathPattern);
        }
        if (pathPattern.contains("*") && !pathPattern.endsWith("/*")) {
            throw new IllegalArgumentException("HTTP filter wildcard pattern must end with '/*': " + pathPattern);
        }
    }

    private static boolean matchesPattern(final String pathPattern, final String path) {
        if (!pathPattern.endsWith("/*")) {
            return pathPattern.equals(path);
        }
        if ("/*".equals(pathPattern)) {
            return path.startsWith("/");
        }
        final String prefix = pathPattern.substring(0, pathPattern.length() - 2);
        return path.equals(prefix) || path.startsWith(prefix + "/");
    }
}
