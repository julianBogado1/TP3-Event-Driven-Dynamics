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
        System.out.println("Wall: "+w.getVertex1()+" to "+w.getVertex2());
        System.out.println("Particle: "+p.getPosition());
        if (w.getOrientation() == Wall.Orientation.HORIZONTAL) {
            // bounce in Y: invert vertical velocity
            p.setVelocity(new Vector(p.getVelocity().getX(), -p.getVelocity().getY()));
        } else if (w.getOrientation() == Wall.Orientation.VERTICAL) {
            // bounce in X: invert horizontal velocity
            p.setVelocity(new Vector(-p.getVelocity().getX(), p.getVelocity().getY()));
        }

    }

    public Particle getParticle() {
        return particle;
    }
    public Wall getWall() {
        return wall;
    }
}
