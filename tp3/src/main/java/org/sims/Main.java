package org.sims;

import org.sims.Simulation.Step;
import org.sims.models.Particle;
import org.sims.models.Wall;
import me.tongfei.progressbar.ProgressBar;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final var walls = Wall.generate(0.07);
        final var particles = Simulation.generateInitialState(200, 0.01, 0.0015);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(particles.size(), 0.07));
            for (final var w : walls) {
                writer.write("%s\n".formatted(w));
            }
        }

        System.out.println("Preparing folder output...");
        Resources.preparePath("steps");

        final var sim = new Simulation(100_000, particles, walls);
        final var engine = sim.engine();

        try (
                final var pb = new ProgressBar("Simulating", sim.steps());
                final var timeout = Resources.writer("events.txt");
                final var animator = Executors.newFixedThreadPool(4);
                final var timer = Executors.newSingleThreadExecutor()) {

            Timer.setOutput(timeout);
            animator.submit(new Animator(engine.initial()));

            for (final var step : engine) {
                animator.submit(new Animator(step));
                timer.submit(new Timer(step.event()));
                pb.step();
            }
        }
    }

    private static record Animator(Step step) implements Runnable {
        @Override
        public void run() {
            final var filename = "%d.txt".formatted(step.i());
            try (final var writer = Resources.writer("steps", filename)) {
                for (final var p : step.particles()) {
                    writer.write("%s\n".formatted(p));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static record Timer(Event event) implements Runnable {
        private static Appendable output;

        @Override
        public void run() {
            try {
                final var type = event.c() instanceof Particle ? "PARTICLE" : "WALL";
                output.append("%.14f %s %d %d\n".formatted(
                    event.time(), 
                    type, 
                    event.p().id(),
                    event.c().id()
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void setOutput(final Appendable output) {
            Timer.output = output;
        }
    }
}
