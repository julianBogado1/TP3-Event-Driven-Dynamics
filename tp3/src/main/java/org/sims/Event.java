package org.sims;

import java.util.stream.Stream;

import org.sims.models.Collideable;
import org.sims.models.Particle;

public record Event(Particle p, Collideable c, double time, long etag)
        implements Comparable<Event>, Cloneable {
    public Event(Particle p, Collideable c, double time) {
        this(p, c, time, Event.etag(p, c));
    }

    /**
     * Generates an event tag based on the number of events each particle has
     *
     * @param p A particle
     * @param c A collideable
     * @return The event tag
     */
    private static long etag(final Particle p, final Collideable c) {
        var etag = p.getEvents();
        if (c instanceof Particle p2) {
            etag += p2.getEvents();
        }
        return etag;
    }

    public boolean valid(double currentTime) {
        return this.time >= currentTime && this.etag == Event.etag(p, c);
    }

    public Stream<Particle> involved() {
        if (c instanceof Particle p2) {
            return Stream.of(p, p2);
        }

        return Stream.of(p);
    }

    @Override
    public int compareTo(Event o) {
        return Double.compare(this.time, o.time);
    }

    @Override
    public Event clone() {
        return new Event(new Particle(p), c.clone(), time, etag);
    }
}
