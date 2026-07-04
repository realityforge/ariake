package org.ariake.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import org.junit.Test;

public final class AriakeConfigTest {
    @Test
    public void readsTypedValues() {
        final var config = AriakeConfig.of(Map.of(
                "name", "ariake",
                "port", "9090",
                "enabled", "true",
                "timeout", "PT2S"));

        assertEquals("ariake", config.require("name"));
        assertEquals(9090, config.getInt("port", 8080));
        assertTrue(config.getBoolean("enabled", false));
        assertEquals(Duration.ofSeconds(2), config.getDuration("timeout", Duration.ZERO));
    }

    @Test
    public void loadsPropertiesFiles() throws IOException {
        final Path file = Files.createTempFile("ariake-config", ".properties");
        try {
            Files.writeString(file, "name=ariake\nport=7070\n");

            final AriakeConfig config = AriakeConfig.load(file);

            assertEquals("ariake", config.require("name"));
            assertEquals(
                    Map.of(
                            "name", "ariake",
                            "port", "7070"),
                    config.asMap());
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void returnsDefaultsForMissingValues() {
        final AriakeConfig config = AriakeConfig.empty();

        assertEquals("fallback", config.get("missing", "fallback"));
        assertEquals(8080, config.getInt("port", 8080));
        assertFalse(config.getBoolean("enabled", false));
        assertEquals(Duration.ofSeconds(5), config.getDuration("timeout", Duration.ofSeconds(5)));
    }

    @Test
    public void reportsMissingRequiredValues() {
        final AriakeConfig config = AriakeConfig.empty();

        final var error = assertThrows(IllegalArgumentException.class, () -> config.require("missing"));
        assertEquals("Missing required config property: missing", error.getMessage());
    }
}
