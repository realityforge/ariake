package org.ariake.examples.hello;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;
import sting.Injector;

@Injector(
        fragmentOnly = false,
        inputs = @Injector.Input(type = Path.class),
        includes = {HelloFragment.class, HealthResponderImpl.class})
public interface HelloApplication {
    static HelloApplication create(final Path configPath) {
        return new Sting_HelloApplication(configPath);
    }

    AriakeServer server();
}
