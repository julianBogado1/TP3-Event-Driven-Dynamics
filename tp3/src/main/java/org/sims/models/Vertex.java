package org.sims.models;

public record Vertex(Vector position, Particle ghost, long id) implements Collideable {
    private static long SERIAL = 0L;

    public Vertex(Vector position) {
        this(position, new Particle(position, new Vector(0, 0), 0), SERIAL++);
    }

    @Override
    public double collisionTime(Particle p) {
        return ghost.collisionTime(p);
    }

    @Override
    public String toString() {
        return "%s".formatted(position);
    }

    @Override
    public String name() {
        return "VERTEX";
    }

    @Override
    public Vertex clone() {
        return new Vertex(position, ghost.clone(), id);
    }
}
