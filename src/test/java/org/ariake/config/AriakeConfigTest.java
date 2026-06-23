package org.ariake.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.Map;
import org.junit.Test;

public final class AriakeConfigTest {
    @Test
    public void readsTypedValues() {
        final AriakeConfig config = AriakeConfig.of(Map.of(
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
    public void reportsMissingRequiredValues() {
        final AriakeConfig config = AriakeConfig.empty();

        final IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> config.require("missing"));
        assertEquals("Missing required config property: missing", error.getMessage());
    }
}
