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


    private static final double MAGIC_NUMBER = 0.09;
    public List<Particle> generateInitialState(int numParticles, double startingVelocity){
        List<Wall> walls = Wall.generate(0.05);
        List<Particle> particles = new ArrayList<>();
        for(int i=0; i<numParticles; i++){
            boolean generated = false;
            double x;
            double y;
            while(!generated){
                x = Math.random() * MAGIC_NUMBER;
                y = Math.random() * MAGIC_NUMBER;
                Particle p = new Particle(new Vector(x,y), new Vector(x,y), radius);
                if(checkValidPosition(p, walls) && checkOverlap(p, particles)) {
                    generated = true;

                }
            }
        }
        return particles;
    }

    private boolean checkValidPosition(Particle p, List<Wall> walls){
        boolean valid = false;
        for(Wall w : walls){
//            check particle inside walls
        }
        return valid;
    }

    private boolean checkOverlap(Particle p, List<Particle> particles){
        boolean overlap = false;
        for(Particle other : particles){
            //check overlap accounting for particles radiuses
        }
        return overlap;
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
}

