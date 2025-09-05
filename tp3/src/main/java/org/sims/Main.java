package org.sims;

import org.sims.models.Particle;
import org.sims.models.Wall;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final var walls = Wall.generate(0.06);
        final var particles = Particle.generateInitialState(20, 0.3, 0.005);

        try (final var writer = Resources.writer("walls.txt")) {
            for (final var w : walls) {
                writer.write(w.toString() + "\n");
            }
        }

        Resources.preparePath("steps");

        final var sim = new Simulation(100, particles, walls);
        try (final var engine = sim.engine(); final var writer = Executors.newSingleThreadExecutor()) {
            for (final var step : engine) {
                final var i = step.i();

                if (i % 5 == 0) {
                    writer.submit(() -> {
                        final var filename = "step_%03d.txt".formatted(i / 5);

                        try (final var step_writer = Resources.writer("steps", filename)) {
                            for (final var p : step.particles()) {
                                step_writer.write(p.toString() + "\n");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        }
    }

    // TODO wall generator that checks closed polygon
}
