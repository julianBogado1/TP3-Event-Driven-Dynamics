//package org.sims;
//
//import java.util.stream.Stream;
//
//import org.sims.models.Collideable;
//import org.sims.models.Particle;
//
//public record Event(Particle p, Collideable c, double time)
//        implements Comparable<Event>, Cloneable {
//    public Stream<Particle> involved() {
//        if (c instanceof final Particle p2) {
//            return Stream.of(p, p2);
//        }
//
//        return Stream.of(p);
//    }
//
//    @Override
//    public int compareTo(Event o) {
//        return Double.compare(this.time, o.time);
//    }
//
//    @Override
//    public Event clone() {
//        return new Event(p.clone(), c.clone(), time);
//    }
//}
