package org.sims;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sims.models.Collideable;
import org.sims.models.Particle;
import org.sims.models.Vector;
import org.sims.models.Vertex;
import org.sims.models.Wall;
import org.sims.models.Wall.Orientation;

/**
 * A simulation of particles in a bounded 2D space with walls.
 *
 * Defines the initial state of the simulation, and provides an engine to run
 * the simulation.
 */
public record Simulation(long steps, double L, List<Particle> particles, List<Collideable> box,
        List<Collideable> collideables) {
    public Simulation(long steps, double L, List<Particle> particles, List<Collideable> box) {
        this(steps, L, List.copyOf(particles), List.copyOf(box),
                Stream.concat(particles.stream(), box.stream()).toList());
    }

    /**
     * Creates a simulation engine that can be used to run the simulation
     *
     * @return The simulation engine
     */
    public Engine engine() {
        return new Engine(this);
    }

    /**
     * Size of the left box, where the particles start
     */
    private static final double MAGIC_NUMBER = 0.09;

    /**
     * Build a simulation with particles of random positions and radii
     *
     * @param steps number of steps to simulate
     * @param L     variable length of the right side
     * @param count number of particles to generate
     * @param vel   initial velocity of particles (x,y components)
     * @param radius radius of particles
     * @return the built simulation
     */
    public static Simulation buildSimulation(long steps, double L, int count, double vel, double radius) {
        final var collideables = generateBox(L);
        final var walls = collideables.stream().filter(c -> c instanceof Wall).map(c -> (Wall) c).toList();
        final var particles = new ArrayList<Particle>(count);

        for (int i = 0; i < count; i++) {
            final var theta = Math.random() * 2 * Math.PI;
            final var velocity = new Vector(vel * Math.cos(theta), vel * Math.sin(theta));

            final var p = new Particle(null, velocity, radius);
            do {
                p.setPosition(new Vector(Math.random() * MAGIC_NUMBER, Math.random() * MAGIC_NUMBER));
            } while (!checkValidPosition(p, walls) || !checkNonOverlap(p, particles));

            particles.add(p);
        }

        if (particles.size() < count) {
            throw new IllegalArgumentException("Radius too big or too many particles");
        }

        return new Simulation(steps, L, particles, collideables);
    }

    /**
     * Generate the contour of the system
     *
     * @param L variable length of the right side
     * @return collision time
     */
    private static List<Collideable> generateBox(double L) {
        final var lilCorner = (0.09 - L) / 2.0;
        final var c = new ArrayList<Collideable>(10);

        c.add(new Wall(Orientation.HORIZONTAL, new Vector(0, 0), new Vector(0.09, 0), 0));
        c.add(new Wall(Orientation.VERTICAL, new Vector(0.09, 0), new Vector(0.09, lilCorner), 1));
        c.add(new Vertex(new Vector(0.09, lilCorner)));
        c.add(new Wall(Orientation.HORIZONTAL, new Vector(0.09, lilCorner), new Vector(0.18, lilCorner), 2));
        c.add(new Wall(Orientation.VERTICAL, new Vector(0.18, lilCorner), new Vector(0.18, lilCorner + L), 3));
        c.add(new Wall(Orientation.HORIZONTAL, new Vector(0.18, lilCorner + L), new Vector(0.09, lilCorner + L), 4));
        c.add(new Vertex(new Vector(0.09, lilCorner + L)));
        c.add(new Wall(Orientation.VERTICAL, new Vector(0.09, lilCorner + L), new Vector(0.09, 0.09), 5));
        c.add(new Wall(Orientation.HORIZONTAL, new Vector(0.09, 0.09), new Vector(0, 0.09), 6));
        c.add(new Wall(Orientation.VERTICAL, new Vector(0, 0.09), new Vector(0, 0), 7));

        return c;
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
