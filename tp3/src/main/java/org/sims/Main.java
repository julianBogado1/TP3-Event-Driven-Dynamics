package org.sims;

import org.sims.Simulation.Step;
import org.sims.models.Wall;
import com.google.gson.Gson;
import java.io.FileReader;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {
    private static final int ANIMATION_INTERVAL = 5;

    public static void main(String[] args) throws Exception {

        Gson gson = new Gson();

        // Read JSON into a Map
        Map<String, Number> data = gson.fromJson(new FileReader("src/main/resources/config.json"), Map.class);
        double radius, velocity, L;
        int N = data.get("particles").intValue();
        radius = data.get("radius").doubleValue();
        velocity = data.get("velocity").doubleValue();
        L = data.get("L").doubleValue();


        final var walls = Wall.generate(L);
        final var particles = Simulation.generateInitialState(N, velocity, radius);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(particles.size(), 0.06));
            for (final var w : walls) {
                writer.write("%s\n".formatted(w));
            }
        }

        Resources.preparePath("steps");

        System.out.println("Starting simulation...");
        final var sim = new Simulation(200, particles, walls);
        final var engine = sim.engine();

        try (final var executor = Executors.newSingleThreadExecutor()) {
            executor.submit(new Animator(engine.initial()));

            for (final var step : engine) {
                if (step.i() % ANIMATION_INTERVAL == 0) {
                    executor.submit(new Animator(step));
                }
            }
        }
    }

    private static record Animator(Step step) implements Runnable {
        @Override
        public void run() {
            final var filename = "%d.txt".formatted(step.i() / ANIMATION_INTERVAL);
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
