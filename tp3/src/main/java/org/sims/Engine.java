package org.sims;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.Stream;

import org.sims.Simulation.Step;
import org.sims.models.Particle;

public record Engine(Simulation simulation) implements Iterable<Step> {
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
            private double time = 0.0;

            @Override
            public boolean hasNext() {
                return current < simulation.steps();
            }

            @Override
            public Step next() {
                final var events = new LinkedList<Event>();

                while (!queue.isEmpty() && !queue.peek().valid(current)) {
                    queue.poll();
                }

                final var event = queue.poll();

                moveAll(event.time() - time /* Time-Skip */);
                time = event.time();

                Particle.collide(event.p(), event.c());

                events.add(event.clone());

                calculateEvents(event.involved(), time, queue);

                if (queue.isEmpty()) {
                    throw new IllegalStateException("No more events to process");
                }

                return new Step(++current, Particle.deepCopy(particles), List.copyOf(events));
            }

            private void moveAll(final double dt) {
                particles.parallelStream().forEach(p -> p.move(dt));
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
        stream
                .map(p -> simulation.collideables().parallelStream()
                        .map(c -> new Event(p, c, c.collisionTime(p)))
                        .min(Event::compareTo)
                        .orElse(null))
                .forEach(e -> queue.add(e));
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
}
