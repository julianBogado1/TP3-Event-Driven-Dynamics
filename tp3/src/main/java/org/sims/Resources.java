package org.sims;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

public abstract class Resources {
    public static final String OUTPUT_PATH = Path.of("src", "main", "resources").toAbsolutePath().toString();

    public static BufferedWriter writer(String... path) throws IOException {
        return new BufferedWriter(new FileWriter(pathed(path).toFile()));
    }

    public static void preparePath(boolean preserve, String... path) {
        final var directory = new File(pathed(path).toString());
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!preserve) {
            for (final var file : directory.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                }
            }
        }
    }

    public static void preparePath(String... path) {
        preparePath(false, path);
    }

    private static Path pathed(String... path) {
        return Path.of(OUTPUT_PATH, path);
    }
}
