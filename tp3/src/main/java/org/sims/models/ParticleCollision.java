package org.sims.models;

public class ParticleCollision extends Event{
    private Particle p1;
    private Particle p2;

    public ParticleCollision(Particle p1, Particle p2, double collisionTime) {
        this.collisionTime = collisionTime;
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public void execute() {
        collide(p1, p2);
    }

    /**
     * Changes velocities of the particles received
     * 
     * @param p1 first particle
     * @param p2 second particle
     */
    public static void collide(Particle p1, Particle p2) {
        final var rvel = p2.getVelocity().subtract(p1.getVelocity());
        final var rpos = p2.getPosition().subtract(p1.getPosition());

        final var vel_pos = rvel.dot(rpos);
        final var dist = p1.getRadius() + p2.getRadius();

        final var impulse = (2 * vel_pos) / (2 * dist);
        final var j = rpos.mult(impulse).div(dist);

        p1.setVelocity(p1.getVelocity().add(j));
        p2.setVelocity(p2.getVelocity().subtract(j));
    }
}
