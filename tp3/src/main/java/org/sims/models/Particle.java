package org.sims.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Particle implements Collideable {
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

    @Override
    public Particle clone() {
        return new Particle(this);
    }

    /**
     * Get unique ID of the particle
     *
     * @return ID of the particle
     */
    public long id() {
        return ID;
    }

    public Vector getVelocity() {
        return velocity;
    }

    public void setVelocity(Vector velocity) {
        this.velocity = velocity;
        addEvent();
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
    @Override
    public double collisionTime(final Particle p) {
        if (p == this) {
            return Double.POSITIVE_INFINITY;
        }

        final var rvel = p.velocity.subtract(this.velocity);
        final var rpos = p.position.subtract(this.position);

        final var vel_pos = rvel.dot(rpos);

        if (vel_pos >= -1e-14) {
            return Double.POSITIVE_INFINITY;
        }

        final var vel_vel = rvel.dot(rvel);

        if (-1e-14 < vel_vel && vel_vel < 1e-14) {
            return Double.POSITIVE_INFINITY;
        }

        final var pos_pos = rpos.dot(rpos);
        final var sigma = this.radius + p.radius;

        final var d = vel_pos * vel_pos - vel_vel * (pos_pos - sigma * sigma);

        if (d < 1e-14) {
            return Double.POSITIVE_INFINITY;
        }

        final var t = -(vel_pos + Math.sqrt(d)) / vel_vel;

        if (t < 1e-14) {
            return Double.POSITIVE_INFINITY;
        }

        return t;
    }

    public static List<Particle> deepCopy(final List<Particle> particles) {
        final var copy = new ArrayList<Particle>(particles.size());
        particles.forEach(p -> copy.add(new Particle(p)));
        return copy;
    }

    /**
     * Changes velocities of this and the particles received
     *
     * @param p second particle
     */
    public void collide(final Particle p) {
        final var rvel = p.velocity.subtract(this.velocity);
        final var rpos = p.position.subtract(this.position);

        final var vel_pos = rvel.dot(rpos);
        final var dist = this.radius + p.radius;

        final var impulse = (2 * vel_pos) / (2 * dist);
        final var j = rpos.mult(impulse).div(dist);

        this.setVelocity(this.velocity.add(j));
        p.setVelocity(p.getVelocity().subtract(j));
    }

    @Override
    public String name() {
        return "PARTICLE";
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
