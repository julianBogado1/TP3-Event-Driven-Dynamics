package org.sims.models;

public interface Collideable extends Cloneable {
    double collisionTime(Particle p);
    Collideable clone();
}
