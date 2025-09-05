package org.sims;

import org.sims.models.Particle;
import org.sims.models.Wall;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        final var walls = Wall.generate(0.06);
        final var particles = Particle.generateInitialState(20, 0.3, 0.005);

        final var environment_writer = Resources.writer("particles.txt");

        environment_writer.write("Walls:\n");
        for (final var w : walls) {
            environment_writer.write(w.toString() + "\n");
        }

        environment_writer.write("Particles:\n");
        for (final var p : particles) {
            environment_writer.write(p.toString() + "\n");
        }

        environment_writer.close();
    }

    // TODO wall generator that checks closed polygon
}
