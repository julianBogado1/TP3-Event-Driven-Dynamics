package org.sims;

import org.sims.models.Particle;
import org.sims.models.Wall;

public record Event(Particle p1, Particle p2, Wall w, double time, long etag)
        implements Comparable<Event>, Cloneable {
    /**
     * Creates a particle-particle collision event
     *
     * @param p1   The first particle
     * @param p2   The second particle
     * @param time The absolute time of the event
     */
    public Event(final Particle p1, final Particle p2, double time) {
        this(p1, p2, null, time, Event.etag(p1, p2));
    }

    /**
     * Creates a particle-wall collision event
     *
     * @param p1   The particle
     * @param w    The wall
     * @param time The absolute time of the event
     */
    public Event(final Particle p1, final Wall w, double time) {
        this(p1, null, w, time, Event.etag(p1, null));
    }

    /**
     * Generates an event tag based on the number of events each particle has
     *
     * @param a A particle
     * @param b Another particle (or null)
     * @return The event tag
     */
    private static long etag(final Particle a, final Particle b) {
        var etag = a.getEvents();
        if (b != null) {
            etag += b.getEvents();
        }
        return etag;
    }

    public boolean isWallCollision() {
        return w != null;
    }

    public boolean isParticleCollision() {
        return p2 != null;
    }

    public boolean valid(double currentTime) {
        return this.time >= currentTime && this.etag == Event.etag(p1, p2);
    }

    @Override
    public int compareTo(Event o) {
        return Double.compare(this.time, o.time);
    }

    @Override
    public Event clone() {
        return new Event(new Particle(p1), p2 != null ? new Particle(p2) : null, w, time, etag);
    }
}
