package org.ariake.examples.health;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;
import sting.Injector;

@Injector(
        fragmentOnly = false,
        inputs = @Injector.Input(type = Path.class),
        includes = {HealthFragment.class, HealthResponderImpl.class})
public interface HealthApplication {
    static HealthApplication create(final Path configPath) {
        return new Sting_HealthApplication(configPath);
    }

    AriakeServer server();
}
