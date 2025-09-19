package org.sims;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public abstract class Resources {
    public static final String OUTPUT_PATH = Path.of("src", "main", "python", "simulations").toAbsolutePath().toString();

    public static BufferedWriter writer(String... path) throws IOException {
        return new BufferedWriter(new FileWriter(pathed(path).toFile()));
    }

    public static void preparePath(boolean preserve, String... path) {
        final var directory = pathed(path).toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!preserve) {
            List.of(directory.listFiles()).parallelStream()
                    .filter(File::isFile)
                    .forEach(File::delete);
        }
    }

    public static void preparePath(String... path) {
        preparePath(false, path);
    }

    private static Path pathed(String... path) {
        return Path.of(OUTPUT_PATH, path);
    }
}
