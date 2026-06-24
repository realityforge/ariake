package org.ariake.examples.staticcontent;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import org.ariake.config.AriakeConfig;
import org.jspecify.annotations.Nullable;

final class StaticContentRoot {
    private static final String STATIC_PREFIX = "/static";
    private static final String RUNFILES_WORKSPACE = "_main";
    private static final Path SOURCE_ROOT = Path.of("examples/static/content");

    private final Path path;

    private StaticContentRoot(final Path path) {
        this.path = path.toAbsolutePath().normalize();
    }

    static StaticContentRoot locate(final AriakeConfig config) {
        return new StaticContentRoot(
                config.find("static.example.staticRoot").map(Path::of).orElseGet(StaticContentRoot::defaultRoot));
    }

    Path path() {
        return path;
    }

    @Nullable
    Path resolveRequestPath(final String requestPath) {
        final String relativePath = relativePath(requestPath);
        final Path resolved = path.resolve(relativePath).normalize();
        return resolved.startsWith(path) ? resolved : null;
    }

    private static Path defaultRoot() {
        final String runfilesDir = System.getenv("RUNFILES_DIR");
        if (null != runfilesDir) {
            final var runfilesRoot = Path.of(runfilesDir, RUNFILES_WORKSPACE, "examples/static/content");
            if (runfilesRoot.toFile().isDirectory()) {
                return runfilesRoot;
            }
        }

        final String javaRunfiles = System.getenv("JAVA_RUNFILES");
        if (null != javaRunfiles) {
            final var runfilesRoot = Path.of(javaRunfiles, RUNFILES_WORKSPACE, "examples/static/content");
            if (runfilesRoot.toFile().isDirectory()) {
                return runfilesRoot;
            }
        }

        return SOURCE_ROOT;
    }

    private static String relativePath(final String requestPath) {
        final String path = requestPath.equals(STATIC_PREFIX) || requestPath.isEmpty() || "/".equals(requestPath)
                ? "/index.nocache.html"
                : requestPath;
        final String suffix;
        if (path.startsWith(STATIC_PREFIX + "/")) {
            suffix = path.substring(STATIC_PREFIX.length() + 1);
        } else if (path.startsWith("/")) {
            suffix = path.substring(1);
        } else {
            suffix = path;
        }
        final String decoded = URLDecoder.decode(suffix, StandardCharsets.UTF_8);
        return decoded.isEmpty() ? "index.nocache.html" : decoded;
    }
}
