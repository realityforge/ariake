package org.ariake.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

public final class AriakeConfig {
    private final Map<String, String> values;

    private AriakeConfig(final Map<String, String> values) {
        this.values = Map.copyOf(values);
    }

    public static AriakeConfig empty() {
        return new AriakeConfig(Map.of());
    }

    public static AriakeConfig load(final Path path) throws IOException {
        final Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
        }
        return from(properties);
    }

    public static AriakeConfig from(final Properties properties) {
        final LinkedHashMap<String, String> values = new LinkedHashMap<>();
        for (String name : properties.stringPropertyNames()) {
            values.put(name, properties.getProperty(name));
        }
        return new AriakeConfig(values);
    }

    public static AriakeConfig of(final Map<String, String> values) {
        return new AriakeConfig(new LinkedHashMap<>(values));
    }

    public Optional<String> find(final String name) {
        return Optional.ofNullable(values.get(Objects.requireNonNull(name)));
    }

    public String require(final String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException("Missing required config property: " + name));
    }

    public String get(final String name, final String defaultValue) {
        return find(name).orElse(defaultValue);
    }

    public int getInt(final String name, final int defaultValue) {
        return find(name).map(value -> Integer.parseInt(value.trim())).orElse(defaultValue);
    }

    public boolean getBoolean(final String name, final boolean defaultValue) {
        return find(name).map(value -> Boolean.parseBoolean(value.trim())).orElse(defaultValue);
    }

    public Duration getDuration(final String name, final Duration defaultValue) {
        return find(name).map(value -> Duration.parse(value.trim())).orElse(defaultValue);
    }

    public Map<String, String> asMap() {
        return values;
    }
}
