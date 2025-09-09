package org.sims.models;

public abstract class Event {
    public double collisionTime;


    public abstract void execute();

    public double getCollisionTime() {
        return collisionTime;
    }

}
