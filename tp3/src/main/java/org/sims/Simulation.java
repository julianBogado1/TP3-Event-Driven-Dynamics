package org.sims;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sims.models.Collideable;
import org.sims.models.Particle;
import org.sims.models.Vector;
import org.sims.models.Wall;

/**
 * A simulation of particles in a bounded 2D space with walls.
 *
 * Defines the initial state of the simulation, and provides an engine to run
 * the simulation.
 *
 * <pre>
 * {@code
 * final var sim = new Simulation(1000, Simulation.generateInitialState(100, 0.01, 0.002), Wall.generate(0.05));
 * try (final var engine = sim.engine()) {
 *     for (final var step : engine) {
 *         // Process each step of the simulation
 *     }
 * }
 * </pre>
 */
public record Simulation(long steps, List<Particle> particles, List<Collideable> walls, List<Collideable> collideables) {
    public Simulation(long steps, List<Particle> particles, List<Collideable> walls) {
        this(steps, List.copyOf(particles), List.copyOf(walls), Stream.concat(particles.stream(), walls.stream()).toList());
    }

    /**
     * Creates a simulation engine that can be used to run the simulation
     *
     * @return The simulation engine
     */
    public Engine engine() {
        return new Engine(this);
    }

    private static final double MAGIC_NUMBER = 0.09;

    /**
     * Generates initial list of particles with random positions and radii
     *
     * @param numParticles     number of particles to generate
     * @param startingVelocity initial velocity of particles -> now used as x,y
     *                         components
     * @return true if valid position
     */
    public static List<Particle> generateInitialState(int numParticles, double startingVelocity, double radius) {
        final var collideables = Wall.generate(0.05);
        final var walls = collideables.stream().filter(c -> c instanceof Wall).map(c -> (Wall) c).toList();
        final var particles = new ArrayList<Particle>(numParticles);

        for (int i = 0; i < numParticles; i++) {
            final var theta = Math.random() * 2 * Math.PI;
            final var velocity = new Vector(startingVelocity * Math.cos(theta), startingVelocity * Math.sin(theta));

            final var p = new Particle(null, velocity, radius);
            do {
                p.setPosition(new Vector(Math.random() * MAGIC_NUMBER, Math.random() * MAGIC_NUMBER));
            } while (checkValidPosition(p, walls) && checkNonOverlap(p, particles));

            particles.add(p);
        }

        if (particles.size() < numParticles) {
            throw new IllegalArgumentException("Radius too big or too many particles");
        }

        return particles;
    }

    /**
     * Checks if a particle is inside boundaries
     * Assumes particles start in a rectangular area
     *
     * @return true if valid position
     */
    private static boolean checkValidPosition(Particle p, List<Wall> walls) {
        Vector pos = p.getPosition();
        double radius = p.getRadius();

        // Check if particle center plus radius is within the bounded area formed by walls
        // For a rectangular boundary, we need to ensure the particle doesn't go outside

        double minX = 0.0, maxX = 0.09;
        double minY = 0.0, maxY = 0.09;

        System.out.println("particle: " + p);
        System.out.println("MinX: " + minX + " MaxX: " + maxX + "MinY:  " + minY + "MaxY:  " + maxY);
        System.out.println(
                "Check X: " + ((pos.x() - radius >= minX) &&
                        (pos.x() + radius <= maxX)) + " Check Y: "
                        + ((pos.y() - radius >= minY) &&
                                (pos.y() + radius <= maxY)));

        // Check if particle (considering its radius) is within bounds
        return (pos.x() - radius >= minX) &&
                (pos.x() + radius <= maxX) &&
                (pos.y() - radius >= minY) &&
                (pos.y() + radius <= maxY);
    }

    private static boolean checkNonOverlap(Particle p, List<Particle> particles) {
        final Vector pos = p.getPosition();
        final double radius = p.getRadius();

        for (final var other : particles) {
            final Vector otherPos = other.getPosition();
            final double otherRadius = other.getRadius();

            // Calculate distance between particle centers
            final double distance = pos.subtract(otherPos).norm();

            // Check if distance is less than sum of radii (overlap condition)
            if (distance < radius + otherRadius) {
                return false; // Overlap detected
            }
        }

        return true; // No overlap
    }

    public record Step(long i, List<Particle> particles, Event event) {
    }
}
