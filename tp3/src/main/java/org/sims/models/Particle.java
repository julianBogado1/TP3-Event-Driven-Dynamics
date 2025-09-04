package org.sims.models;

import java.util.ArrayList;
import java.util.List;

public class Particle {
    Vector position;
    Vector velocity;
    double radius;
    public Particle(Vector position,  Vector velocity, double radius) {
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
     * @param p description of the parameter
     * @return time of collision from now -> infinity if particles dont collide
     */
    public double collisionTime(Particle p){
        double relativeVelocityX = p.velocity.getX() - this.velocity.getX();
        double relativeVelocityY = p.velocity.getY() - this.velocity.getY();
        double relativePositionX = p.position.getX() - this.position.getX();
        double relativePositionY = p.position.getY() - this.position.getY();
        double sigma = this.radius + p.radius;

        Vector relativeVelocity = new Vector(relativeVelocityX, relativeVelocityY);
        Vector relativePosition = new Vector(relativePositionX, relativePositionY);

        double d = Math.pow(relativeVelocity.dot(relativePosition), 2) -
                relativeVelocity.dot(relativeVelocity) *
                (relativePosition.dot(relativePosition) - sigma * sigma);
        if (d < 0 || relativeVelocity.dot(relativePosition) >= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return -(relativeVelocity.dot(relativePosition) + Math.sqrt(d)) / relativeVelocity.dot(relativeVelocity);
    }

    /**
     * Compute timo 🤠 of collision with all walls
     * @return smallest collision time
     */
    public double collisionTimeWithWalls(List<Wall> walls){
        double tc = Double.POSITIVE_INFINITY;
        for(Wall w : walls){
            double t = w.collidesWith(this);
            if(t < tc){
                tc = t;
            }
        }
        return tc;
    }

    /**
     * Generates initial list of particles with random positions and radii
     * @param numParticles number of particles to generate
     * @param startingVelocity initial velocity of particles -> now used as x,y components
     * @return true if valid position
     */
    private static final double MAGIC_NUMBER = 0.09;
    public static List<Particle> generateInitialState(int numParticles, double startingVelocity, double radius){
        List<Wall> walls = Wall.generate(0.05);
        List<Particle> particles = new ArrayList<>();
        for(int i=0; i<numParticles; i++){
            boolean generated = false;
            double x, y;
            while(!generated){
                x = Math.random() * MAGIC_NUMBER;
                y = Math.random() * MAGIC_NUMBER;
//                radius = Math.random() * MAGIC_NUMBER;
                Particle p = new Particle(new Vector(x,y), new Vector(startingVelocity,startingVelocity), radius); //TODO random velocity direction??
                if(checkValidPosition(p, walls) && checkNonOverlap(p, particles)) {
                    generated = true;
                    particles.add(p); // Add the particle to the list
                }
            }
        }
        if(particles.size() < numParticles){throw new IllegalArgumentException("Radius too big or too many particles");}
        return particles;
    }

    /**
     * Checks if a particle is inside boundaries
     * Assumes particles start in a rectangular area
     * @return true if valid position
     */
    private static boolean checkValidPosition(Particle p, List<Wall> walls){
        Vector pos = p.getPosition();
        double radius = p.getRadius();

        // Check if particle center plus radius is within the bounded area formed by walls
        // For a rectangular boundary, we need to ensure the particle doesn't go outside

        // Find the bounding box of the walls
        double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE;
        double minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

        for(Wall w : walls){
            minX = Math.min(minX, Math.min(w.vertex1.getX(), w.vertex2.getX()));
            maxX = Math.max(maxX, Math.max(w.vertex1.getX(), w.vertex2.getX()));
            minY = Math.min(minY, Math.min(w.vertex1.getY(), w.vertex2.getY()));
            maxY = Math.max(maxY, Math.max(w.vertex1.getY(), w.vertex2.getY()));
        }

        // Check if particle (considering its radius) is within bounds
        return (pos.getX() - radius >= minX) &&
               (pos.getX() + radius <= maxX) &&
               (pos.getY() - radius >= minY) &&
               (pos.getY() + radius <= maxY);
    }

    private static boolean checkNonOverlap(Particle p, List<Particle> particles){
        Vector pos = p.getPosition();
        double radius = p.getRadius();

        for(Particle other : particles){
            Vector otherPos = other.getPosition();
            double otherRadius = other.getRadius();

            // Calculate distance between particle centers
            double dx = pos.getX() - otherPos.getX();
            double dy = pos.getY() - otherPos.getY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Check if distance is less than sum of radii (overlap condition)
            if(distance < radius + otherRadius){
                return false; // Overlap detected
            }
        }
        return true; // No overlap
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Particle particle = (Particle) o;
        return Double.compare(particle.radius, radius) == 0 &&
                position.equals(particle.position) &&
                velocity.equals(particle.velocity);
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s", position, velocity, radius);
    }
}
