package org.sims;

import org.sims.Simulation.Step;
import org.sims.models.Wall;
import me.tongfei.progressbar.ProgressBar;

import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) throws Exception {
        final var sim = Simulation.buildSimulation(args[0], args[1], args[2]);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(sim.particles().size(), sim.L()));
            for (final var w : sim.box()) {
                if (w instanceof Wall) {
                    writer.write("%s\n".formatted(w));
                }
            }
        }

        System.out.println("Preparing folder output...");
        Resources.preparePath("steps");

        final var engine = sim.engine();

        try (
                final var pba = new ProgressBar("Saving State", sim.steps() + 1);
                final var animator = Executors.newFixedThreadPool(32);
                final var pbt = new ProgressBar("Saving Event", sim.steps());
                final var timeout = Resources.writer("events.txt");
                final var timer = Executors.newSingleThreadExecutor();
                final var pb = new ProgressBar("Simulating", sim.steps())) {

            Timer.setOutput(timeout);
            animator.submit(new Animator(engine.initial(), pba));

            for (final var step : engine) {
                animator.submit(new Animator(step, pba));
                timer.submit(new Timer(step.event(), pbt));
                pb.step();
            }
        }
    }

    private static record Animator(Step step, ProgressBar pb) implements Runnable {
        @Override
        public void run() {
            final var filename = "%d.txt".formatted(step.i());
            try (final var writer = Resources.writer("steps", filename)) {
                for (final var p : step.particles()) {
                    writer.write("%s\n".formatted(p));
                }
                pb.step();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static record Timer(Event event, ProgressBar pb) implements Runnable {
        private static Appendable output;

        @Override
        public void run() {
            try {
                output.append("%s\n".formatted(event));
                pb.step();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void setOutput(final Appendable output) {
            Timer.output = output;
        }
    }
}
