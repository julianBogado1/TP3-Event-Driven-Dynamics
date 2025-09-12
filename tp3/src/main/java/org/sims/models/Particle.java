package org.sims.models;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

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
    @Override
    public double collisionTime(final Particle p) {
        if (p == this) {
            return Double.POSITIVE_INFINITY;
        }

        final var rvel = p.velocity.subtract(this.velocity);
        final var rpos = p.position.subtract(this.position);
        
        final var vel_pos = rvel.dot(rpos);

        if (vel_pos >= 0) {
            return Double.POSITIVE_INFINITY;
        }

        final var vel_vel = rvel.dot(rvel);

        if (vel_vel == 0) {
            return Double.POSITIVE_INFINITY;
        }

        final var pos_pos = rpos.dot(rpos);
        final var sigma = this.radius + p.radius;

        final var d = vel_pos * vel_pos - vel_vel * (pos_pos - sigma * sigma);

        if (d < 0) {
            return Double.POSITIVE_INFINITY;
        }

        final var t = -(vel_pos + Math.sqrt(d)) / vel_vel;

        if (t < 0) {
            return Double.POSITIVE_INFINITY;
        }

        return t;
    }

    public static List<Particle> deepCopy(final List<Particle> particles) {
        final var copy = new ArrayList<Particle>(particles.size());
        particles.forEach(p -> copy.add(new Particle(p)));
        return copy;
    }

    private static double ct = 1, cn = 1;

    public static void collide(Particle p, Collideable c) {
        if (c instanceof Particle p2) {
            collide(p, p2);
        } else if (c instanceof Wall w) {
            collide(p, w);
        } else {
            throw new IllegalArgumentException("Unknown Collideable type");
        }
    }

    /**
     * Changes velocities of the particles received
     * 
     * @param p     first particle
     * @param other second particle
     */
    public static void collide(Particle p, Particle other) {
        Vector normalVersor = Vector.subtract(p.getPosition(), other.getPosition());
        Vector xVersor = new Vector(1, 0);
        double alpha = Vector.angle(normalVersor, xVersor); // angle between normal versor of collision and x axis
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);
        double m11 = (-cn * cosAlpha * cosAlpha) + (ct * sinAlpha * sinAlpha);
        double m12 = -(cn + ct) * sinAlpha * cosAlpha;
        double m21 = m12;
        double m22 = (-cn * sinAlpha * sinAlpha) + (ct * cosAlpha * cosAlpha);
        double[][] m = new double[][] { { m11, m12 },
                { m21, m22 } };
        RealMatrix collisionOperator = MatrixUtils.createRealMatrix(m);

        // =======First particle=======
        RealMatrix v1 = MatrixUtils.createRealMatrix(p.getVelocity().toColumnMatrix());
        double[] v1Prime = collisionOperator.multiply(v1).getColumn(0);
        p.setVelocity(new Vector(v1Prime[0], v1Prime[1]));

        // =======Second particle=====
        RealMatrix v2 = MatrixUtils.createRealMatrix(other.getVelocity().toColumnMatrix());
        double[] v2Prime = collisionOperator.multiply(v2).getColumn(0);
        other.setVelocity(new Vector(v2Prime[0], v2Prime[1]));

        p.addEvent();
        other.addEvent();
    }

    public static void collide(Particle p, Wall w) {
        switch (w.orientation()) {
            case HORIZONTAL:
                p.setVelocity(new Vector(p.getVelocity().x(), -p.getVelocity().y()));
                break;
            case VERTICAL:
                p.setVelocity(new Vector(-p.getVelocity().x(), p.getVelocity().y()));
                break;
            default:
                throw new IllegalArgumentException("Unsuported wall orientation");
        }

        p.addEvent();
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
