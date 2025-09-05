package org.sims;

import java.util.Iterator;
import java.util.List;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.random.RandomGenerator;

import org.sims.models.Particle;
import org.sims.models.Wall;

/**
 * A simulation of particles in a bounded 2D space with walls.
 *
 * Defines the initial state of the simulation, and provides an engine to run
 * the simulation.
 *
 * <pre>
 * {@code
 * final var sim = new Simulation(1000, Particle.generateInitialState(100, 0.01, 0.002), Wall.generate(0.05));
 * try (final var engine = sim.engine()) {
 *     for (final var step : engine) {
 *         // Process each step of the simulation
 *     }
 * }
 * </pre>
 */
public record Simulation(long steps, List<Particle> particles, List<Wall> walls) {
    private static final RandomGenerator random = new SplittableRandom();

    /**
     * Creates a simulation engine that can be used to run the simulation
     *
     * @apiNote This engine uses a fixed thread pool with a number of threads equal
     *          to the number of available processors.
     *
     * @return The simulation engine
     */
    public Engine engine() {
        return new Engine(this, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    public record Engine(Simulation simulation, ExecutorService executor) implements Iterable<Step>, AutoCloseable {
        @Override
        public Iterator<Step> iterator() {
            return new Iterator<Step>() {
                private List<Particle> particles = List.copyOf(simulation.particles());
                private long current = 0;

                @Override
                public boolean hasNext() {
                    return current < simulation.steps();
                }

                @Override
                public Step next() {
                    particles.parallelStream().forEach(Particle::move);
                    return new Step(current++, List.copyOf(particles), null);
                }
            };
        }

        @Override
        public void close() {
            executor.close();
        }
    }

    public record Event(Particle a, Particle b, double time) {
    }

    public record Step(long i, List<Particle> particles, List<Event> events) {
    }
}
