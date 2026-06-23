package org.ariake.examples.staticcontent;

import java.nio.file.Path;
import org.ariake.server.AriakeServer;
import sting.Injector;

@Injector(fragmentOnly = false, inputs = @Injector.Input(type = Path.class), includes = StaticContentFragment.class)
public interface StaticContentApplication {
    static StaticContentApplication create(final Path configPath) {
        return new Sting_StaticContentApplication(configPath);
    }

    AriakeServer server();
}
