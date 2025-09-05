package org.sims;

import org.sims.models.Particle;
import org.sims.models.Wall;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        final var walls = Wall.generate(0.06);
        final var particles = Particle.generateInitialState(20, 0.3, 0.005);

        try (final var writer = Resources.writer("particles.txt")) {
            writer.write("Walls:\n");
            for (final var w : walls) {
                writer.write(w.toString() + "\n");
            }

            writer.write("Particles:\n");
            for (final var p : particles) {
                writer.write(p.toString() + "\n");
            }
        }
    }

    // TODO wall generator that checks closed polygon
}
