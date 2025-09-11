package org.sims.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Particle {
    private static long SERIAL = 0L;
    private final long ID;

    private Vector position;
    private Vector velocity;
    private double radius;
    private long events = 0;

    public Particle(Vector position, Vector velocity, double radius) {
        this.ID = SERIAL++;
        this.radius = radius;
        this.position = position;
        this.velocity = velocity;
    }

    public Particle(Particle p) {
        this.ID = p.ID;
        this.radius = p.radius;
        this.position = p.position;
        this.velocity = p.velocity;
        this.events = p.events;
    }

    /**
     * Get unique ID of the particle
     *
     * @return ID of the particle
     */
    public long getID() {
        return ID;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
    }

    /**
     * Move particle according to its velocity a delta time
     *
     * @param dt time step
     */
    public void move(double dt) {
        setPosition(Particle.move(this, dt));
    }

    /**
     * Returns the Vector position of the particle if moved a delta time
     *
     * @param p  particle to move
     * @param dt time step
     * @return new position of the particle
     */
    public static Vector move(Particle p, double dt) {
        return p.getPosition().add(p.getVelocity().mult(dt));
    }

    /**
     * Move particle according to its velocity a delta time of 1
     *
     * @deprecated use {@link #move(double)} instead
     * @see #move(double)
     */
    @Deprecated
    public void move() {
        move(0.001);
    }

    public long getEvents() {
        return events;
    }

    private void addEvent() {
        this.events++;
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
    public ParticleCollision collisionTime(final Particle p) {
        if (p == this) {
            return new ParticleCollision(this, p, Double.POSITIVE_INFINITY);
        }

        final var rvel = p.velocity.subtract(this.velocity);
        final var rpos = p.position.subtract(this.position);
        
        final var vel_pos = rvel.dot(rpos);

        if (vel_pos > 0) {
            return new ParticleCollision(this, p, Double.POSITIVE_INFINITY);
        }

        final var vel_vel = rvel.dot(rvel);
        
        if (vel_vel == 0) {
            return new ParticleCollision(this, p, Double.POSITIVE_INFINITY);
        }

        final var pos_pos = rpos.dot(rpos);
        final var sigma = this.radius + p.radius;

        final var d = vel_pos * vel_pos - vel_vel * (pos_pos - sigma * sigma);

        if (d < 0) {
            return new ParticleCollision(this, p, Double.POSITIVE_INFINITY);
        }

        final var t = -(vel_pos + Math.sqrt(d)) / vel_vel;
        
        if (t < 0) {
            return new ParticleCollision(this, p, Double.POSITIVE_INFINITY);
        }

        return new ParticleCollision(this, p, t);
    }

    /**
     * Compute timo 🤠 of collision with all walls
     *
     * @return smallest collision time
     */
    public WallCollision collisionTime(final Wall wall) {
        return new WallCollision(this, wall, wall.collisionTime(this));
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
            double xPos, yPos, xVel, yVel, theta;
            while (!generated) {
                xPos = Math.random() * MAGIC_NUMBER;
                yPos = Math.random() * MAGIC_NUMBER;

                theta = Math.random() * 2 * Math.PI;
                xVel = startingVelocity * Math.cos(theta);
                yVel = startingVelocity * Math.sin(theta);

                final var p = new Particle(new Vector(xPos, yPos), new Vector(xVel, yVel), radius);
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
        return String.format("%s %s %.14f", position, velocity, radius);
    }
}
