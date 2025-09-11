package org.sims;

import org.sims.Simulation.Step;
import org.sims.models.Wall;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final var walls = Wall.generate(0.07);
        final var particles = Simulation.generateInitialState(200, 0.01, 0.0015);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(particles.size(), 0.06));
            for (final var w : walls) {
                writer.write("%s\n".formatted(w));
            }
        }

        Resources.preparePath("steps");

        System.out.println("Starting simulation...");
        final var sim = new Simulation(10_000, particles, walls);
        final var engine = sim.engine();

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(new Animator(engine.initial()));

            for (final var step : engine) {
                executor.submit(new Animator(step));
            }
        }
    }

    private static record Animator(Step step) implements Runnable {
        @Override
        public void run() {
            final var filename = "%d.txt".formatted(step.i());
            try (final var writer = Resources.writer("steps", filename)) {
                for (final var p : step.particles()) {
                    writer.write(p.toString() + "\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
