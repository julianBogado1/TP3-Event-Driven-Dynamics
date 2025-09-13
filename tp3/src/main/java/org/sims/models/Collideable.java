package org.sims.models;

public interface Collideable extends Cloneable {
    double collisionTime(final Particle p);
    void collide(final Particle p);
    long id();
    String name();
    Collideable clone();
}
