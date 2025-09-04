package org.sims.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Particle {
    private Vector position;
    private Vector velocity;
    private double radius;

    public Particle(Vector position, Vector velocity, double radius) {
        this.radius = radius;
        this.position = position;
        this.velocity = velocity;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    public Vector getPosition() {
        return position;
    }

    public void setPosition(Vector position) {
        this.position = position;
    }

    public double getRadius() {
        return radius;
    }

    /**
     * Compute timo of collision of this particle with another particle
     *
     * @param p description of the parameter
     * @return time of collision from now -> infinity if particles dont collide
     */
    public double collisionTime(final Particle p) {
        final double relativeVelocityX = p.velocity.getX() - this.velocity.getX();
        final double relativeVelocityY = p.velocity.getY() - this.velocity.getY();
        final double relativePositionX = p.position.getX() - this.position.getX();
        final double relativePositionY = p.position.getY() - this.position.getY();
        final double sigma = this.radius + p.radius;

        final Vector relativeVelocity = new Vector(relativeVelocityX, relativeVelocityY);
        final Vector relativePosition = new Vector(relativePositionX, relativePositionY);

        final double d = Math.pow(relativeVelocity.dot(relativePosition), 2) -
                relativeVelocity.dot(relativeVelocity) *
                        (relativePosition.dot(relativePosition) - sigma * sigma);

        if (d < 0 || relativeVelocity.dot(relativePosition) >= 0) {
            return Double.POSITIVE_INFINITY;
        }

        return -(relativeVelocity.dot(relativePosition) + Math.sqrt(d)) / relativeVelocity.dot(relativeVelocity);
    }

    /**
     * Compute timo 🤠 of collision with all walls
     *
     * @return smallest collision time
     */
    public double collisionTimeWithWalls(final List<Wall> walls) {
        return walls.parallelStream().map(w -> w.collidesWith(this)).min(Double::compareTo).orElse(Double.POSITIVE_INFINITY);
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
        final List<Wall> walls = Wall.generate(0.05);
        final List<Particle> particles = new ArrayList<>(numParticles);

        for (int i = 0; i < numParticles; i++) {
            boolean generated = false;
            double x, y;
            while (!generated) {
                x = Math.random() * MAGIC_NUMBER;
                y = Math.random() * MAGIC_NUMBER;
                // radius = Math.random() * MAGIC_NUMBER;

                // TODO random velocity direction??
                final var p = new Particle(new Vector(x, y), new Vector(startingVelocity, startingVelocity), radius);
                if (checkValidPosition(p, walls) && checkNonOverlap(p, particles)) {
                    generated = true;
                    particles.add(p); // Add the particle to the list
                }
            }
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
                "Check X: " + ((pos.getX() - radius >= minX) &&
                        (pos.getX() + radius <= maxX)) + " Check Y: "
                        + ((pos.getY() - radius >= minY) &&
                                (pos.getY() + radius <= maxY)));

        // Check if particle (considering its radius) is within bounds
        return (pos.getX() - radius >= minX) &&
                (pos.getX() + radius <= maxX) &&
                (pos.getY() - radius >= minY) &&
                (pos.getY() + radius <= maxY);
    }

    private static boolean checkNonOverlap(Particle p, List<Particle> particles) {
        final Vector pos = p.getPosition();
        final double radius = p.getRadius();

        for (final var other : particles) {
            final Vector otherPos = other.getPosition();
            final double otherRadius = other.getRadius();

            // Calculate distance between particle centers
            final double dx = pos.getX() - otherPos.getX();
            final double dy = pos.getY() - otherPos.getY();
            final double distance = Math.sqrt(dx * dx + dy * dy);

            // Check if distance is less than sum of radii (overlap condition)
            if (distance < radius + otherRadius) {
                return false; // Overlap detected
            }
        }

        return true; // No overlap
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || !(o instanceof Particle other))
            return false;

        return Double.compare(other.radius, radius) == 0 &&
                position.equals(other.position) &&
                velocity.equals(other.velocity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, velocity, radius);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", position, velocity, radius);
    }
}
