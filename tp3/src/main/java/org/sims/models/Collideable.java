package org.sims.models;

public interface Collideable extends Cloneable {
    double collisionTime(Particle p);
    void collide(Particle p);
    long id();
    String name();
    Collideable clone();
}
