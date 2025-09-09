package org.sims;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.SplittableRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

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
        public Step initial() {
            return new Step(0, List.copyOf(simulation.particles()), null);
        }

        @Override
        public Iterator<Step> iterator() {
            final var queue = new PriorityBlockingQueue<Event>();
            calculateEvents(queue);

            return new Iterator<Step>() {
                private List<Particle> particles = List.copyOf(simulation.particles());
                private long current = 0;

                @Override
                public boolean hasNext() {
                    return current < simulation.steps();
                }

                @Override
                public Step next() {
                    final var events = new LinkedList<Event>();

                    var dt = current * 0.01;
                    final var end_dt = dt + 0.01;

                    while (!queue.isEmpty() && queue.peek().time() <= end_dt) {
                        final var event = queue.poll();

                        if (!event.valid(dt)) {
                            continue;
                        }

                        moveAll(event.time() - dt /* Time-Skip */);
                        dt = event.time();

                        if (event.isWallCollision()) {
                            Particle.collide(event.p1(), event.w());
                        } else {
                            Particle.collide(event.p1(), event.p2());
                        }

                        events.add(event.clone());

                        final var involved = Stream.of(event.p1(), event.p2());
                        calculateEvents(involved, dt, queue);
                    }

                    if (queue.isEmpty()) {
                        throw new IllegalStateException("No more events to process");
                    }

                    moveAll(end_dt - dt /* Remainder of time */);

                    return new Step(++current, deepCopy(particles), List.copyOf(events));
                }

                private void moveAll(final double dt) {
                    particles.stream().forEach(p -> p.move(dt));
                }

                private List<Particle> deepCopy(final List<Particle> particles) {
                    final var copy = new ArrayList<Particle>(particles.size());
                    particles.forEach(p -> copy.add(new Particle(p)));
                    return copy;
                }
            };
        }

        /**
         * Populates the queue with collision events for a list of particles
         *
         * @param stream Stream of particles to compute collision events for
         * @param dt     Current simulation time
         * @param queue  Queue to add computed events to
         * @return List of tasks
         */
        private void calculateEvents(final Stream<Particle> stream, double dt, final Queue<Event> queue) {
            final var walls = simulation.walls();
            final var particles = simulation.particles();

            final var tasks = stream
                    .filter(Objects::nonNull)
                    .map(p -> Executors.callable(new ParticleCollider(p, particles, walls, dt, queue)))
                    .toList();

            try {
                executor.invokeAll(tasks);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Simulation interrupted", e);
            }
        }

        /**
         * Populates the queue with collision events for all particles
         *
         * @param queue Queue to add computed events to
         * @return List of tasks
         */
        private void calculateEvents(final Queue<Event> queue) {
            calculateEvents(simulation.particles().parallelStream(), 0.0, queue);
        }

        @Override
        public void close() {
            executor.close();
        }

        /**
         * A thread task to computes collision events for a single particle against a
         * list of particles and walls, and adds them to the output queue.
         */
        private record ParticleCollider(Particle self, List<Particle> particles, List<Wall> walls, double dt,
                Queue<Event> output)
                implements Runnable {
            @Override
            public void run() {
                particles.stream().forEach(p -> {
                    final var t = self.collisionTime(p);
                    if (t != Double.POSITIVE_INFINITY)
                        output.add(new Event(self, p, t + dt));
                });

                walls.stream().forEach(w -> {
                    final var t = self.collisionTime(w);
                    if (t != Double.POSITIVE_INFINITY)
                        output.add(new Event(self, w, t + dt));
                });
            }
        }
    }

    public record Event(Particle p1, Particle p2, Wall w, double time, long etag)
            implements Comparable<Event>, Cloneable {
        /**
         * Creates a particle-particle collision event
         *
         * @param p1   The first particle
         * @param p2   The second particle
         * @param time The absolute time of the event
         */
        public Event(final Particle p1, final Particle p2, double time) {
            this(p1, p2, null, time, Event.etag(p1, p2));
        }

        /**
         * Creates a particle-wall collision event
         *
         * @param p1   The particle
         * @param w    The wall
         * @param time The absolute time of the event
         */
        public Event(final Particle p1, final Wall w, double time) {
            this(p1, null, w, time, Event.etag(p1, null));
        }

        /**
         * Generates an event tag based on the number of events each particle has
         *
         * @param a A particle
         * @param b Another particle (or null)
         * @return The event tag
         */
        private static long etag(final Particle a, final Particle b) {
            var etag = a.getEvents();
            if (b != null) {
                etag += b.getEvents();
            }
            return etag;
        }

        public boolean isWallCollision() {
            return w != null;
        }

        public boolean isParticleCollision() {
            return p2 != null;
        }

        public boolean valid(double currentTime) {
            return this.time >= currentTime && this.etag == Event.etag(p1, p2);
        }

        @Override
        public int compareTo(Event o) {
            return Double.compare(this.time, o.time);
        }

        @Override
        public Event clone() {
            return new Event(new Particle(p1), p2 != null ? new Particle(p2) : null, w, time, etag);
        }
    }

    public record Step(long i, List<Particle> particles, List<Event> events) {
    }
}
