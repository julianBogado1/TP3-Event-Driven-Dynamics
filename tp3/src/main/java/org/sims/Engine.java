package org.sims;

import java.util.Iterator;
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
                return !queue.isEmpty() && current < simulation.steps();
            }

            @Override
            public Step next() {
                while (!queue.peek().valid(time)) {
                    queue.poll();
                }

                final var event = queue.poll();

                if (event.time() == Double.POSITIVE_INFINITY) {
                    System.err.println("Huh");
                }

                moveTo(event);
                time = event.time();

                Particle.collide(event.p(), event.c());
                calculateEvents(event.involved(), time, queue);

                return new Step(++current, Particle.deepCopy(particles), event.clone());
            }

            private void moveTo(final Event e) {
                particles.parallelStream().forEach(p -> p.move(e.time() - time));
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
    private void calculateEvents(final Stream<Particle> stream, final double dt, final Queue<Event> queue) {
        stream.map(p -> nextEvents(p, dt)).forEach(e -> queue.addAll(e));
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

    private List<Event> nextEvents(final Particle p, final double dt) {
        return simulation.collideables().parallelStream()
                .map(c -> new Event(p, c, c.collisionTime(p) + dt))
                .filter(e -> e.time() < Double.POSITIVE_INFINITY)
                .toList();
    }
}
