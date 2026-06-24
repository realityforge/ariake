package org.ariake.examples.jpa;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;
import sting.Injector;

@Injector(fragmentOnly = false, inputs = @Injector.Input(type = Path.class), includes = JpaFragment.class)
interface JpaApplication {
    static JpaApplication create(final Path configPath) {
        return new Sting_JpaApplication(configPath);
    }

    AriakeServer server();
}
