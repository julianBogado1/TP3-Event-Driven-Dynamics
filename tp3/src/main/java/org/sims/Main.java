package org.sims;

import org.sims.Simulation.Step;
import org.sims.models.Particle;
import org.sims.models.Wall;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final var walls = Wall.generate(0.06);
        final var particles = Particle.generateInitialState(20, 0.01, 0.005);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(particles.size(), 0.06));
            for (final var w : walls) {
                writer.write("%s\n".formatted(w));
            }
        }

        Resources.preparePath("steps");

        System.out.println("Starting simulation...");
        final var sim = new Simulation(10_000, particles, walls);
        try (final var engine = sim.engine(); final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(new Animator(engine.initial()));

            for (final var step : engine) {
                if (step.i() % 5 == 0) {
                    executor.submit(new Animator(step));
                }
            }
        }
    }

    private static record Animator(Step step) implements Runnable {
        @Override
        public void run() {
            final var filename = "%d.txt".formatted(step.i() / 5);
            try (final var writer = Resources.writer("steps", filename)) {
                for (final var p : step.particles()) {
                    writer.write(p.toString() + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TODO wall generator that checks closed polygon
}
