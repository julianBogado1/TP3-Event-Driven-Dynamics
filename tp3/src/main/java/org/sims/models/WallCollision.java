package org.sims.models;

public class WallCollision extends Event{

    private Particle particle;
    private Wall wall;

    public WallCollision(Particle particle, Wall wall, double collisionTime) {
        this.collisionTime = collisionTime;
        this.particle = particle;
        this.wall = wall;
    }


    @Override
    public void execute() {
        collide(particle, wall);
    }

    private void collide(Particle p, Wall w) {
        if(w.getVertex1().getX()-w.getVertex2().getX()<=0){ //horizontal wall
            p.setVelocity(new Vector(p.getVelocity().getX(), -p.getVelocity().getY()));
        }
        else if(w.getVertex1().getY()-w.getVertex2().getY()<=0){ //vertical wall
            p.setVelocity(new Vector(-p.getVelocity().getX(), p.getVelocity().getY()));
        }

    }
}
