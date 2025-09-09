package org.sims.models;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

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
        setPosition(this.position.add(this.velocity.mult(dt)));
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
    public double collisionTime(final Particle p) {
        if (p == this) {
            return Double.POSITIVE_INFINITY;
        }

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
    public double collisionTime(final Wall wall) {
        return wall.collidesWith(this);
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



    private static double ct = 1, cn = 1;
    /**
     * Changes velocities of the particles received
     * @param p first particle
     * @param other second particle
     */
    public static void collide(Particle p, Particle other) {
        Vector normalVersor = Vector.subtract(p.getPosition(), other.getPosition());
        Vector xVersor = new Vector(1,0);
        double alpha = Vector.angle(normalVersor, xVersor);    //angle between normal versor of collision and x axis
        double cosAlpha = Math.cos(alpha);
        double sinAlpha = Math.sin(alpha);
        double m11 = (-cn * cosAlpha*cosAlpha) + (ct * sinAlpha*sinAlpha);
        double m12 = -(cn+ct)*sinAlpha*cosAlpha;
        double m21 = m12;
        double m22 = (-cn * sinAlpha*sinAlpha) + (ct * cosAlpha*cosAlpha);
        double[][] m = new double[][]{  {m11, m12},
                                        {m21, m22} };
        RealMatrix collisionOperator = MatrixUtils.createRealMatrix(m);

        //=======First particle=======
        RealMatrix v1 = MatrixUtils.createRealMatrix(p.getVelocity().toColumnMatrix());
        double[] v1Prime = collisionOperator.multiply(v1).getColumn(0);
        p.setVelocity(new Vector(v1Prime[0], v1Prime[1]));

        //=======Second particle=====
        RealMatrix v2 = MatrixUtils.createRealMatrix(other.getVelocity().toColumnMatrix());
        double[] v2Prime = collisionOperator.multiply(v2).getColumn(0);
        other.setVelocity(new Vector(v2Prime[0], v2Prime[1]));

        p.addEvent();
        other.addEvent();
    }

    public static void collide(Particle p, Wall w) {
        if(w.getVertex1().getX()-w.getVertex2().getX()<=0){ //horizontal wall
            p.setVelocity(new Vector(p.getVelocity().getX(), -p.getVelocity().getY()));
        }
        else if(w.getVertex1().getY()-w.getVertex2().getY()<=0){ //vertical wall
            p.setVelocity(new Vector(-p.getVelocity().getX(), p.getVelocity().getY()));
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
