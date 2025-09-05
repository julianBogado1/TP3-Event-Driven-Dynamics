package org.sims.models;

import java.util.ArrayList;
import java.util.List;

public class Wall {
    private final Vector vertex1;
    private final Vector vertex2;

    public Wall(final Vector v1, final Vector v2) {
        this.vertex1 = v1;
        this.vertex2 = v2;
    }

    /**
     * Generate the contour of the system
     *
     * @param L variable length of the right side
     * @return collision time
     */
    public static List<Wall> generate(double L) {
        final double lilCorner = (0.09 - L) / 2.0;
        final List<Wall> walls = new ArrayList<>(8);

        walls.add(new Wall(new Vector(0, 0), new Vector(0.09, 0)));
        walls.add(new Wall(new Vector(0.09, 0), new Vector(0.09, lilCorner)));
        walls.add(new Wall(new Vector(0.09, lilCorner), new Vector(0.18, lilCorner)));
        walls.add(new Wall(new Vector(0.18, lilCorner), new Vector(0.18, lilCorner + L)));
        walls.add(new Wall(new Vector(0.18, lilCorner + L), new Vector(0.09, lilCorner + L)));
        walls.add(new Wall(new Vector(0.09, lilCorner + L), new Vector(0.09, 0.09)));
        walls.add(new Wall(new Vector(0.09, 0.09), new Vector(0, 0.09)));
        walls.add(new Wall(new Vector(0, 0.09), new Vector(0, 0)));

        return walls;
    }

    /**
     * Compute timo of the wall with the particle
     *
     * @param p particle to check collision with
     * @return collision time
     */
    public double collidesWith(final Particle p) {
        final double pVelocity;
        final double pCurrentPos;

        if (Math.abs(vertex1.getX() - vertex2.getX()) < 0.00001) { // vertical wall
            final double wallX = vertex1.getX();

            pVelocity = p.getVelocity().getX();
            pCurrentPos = p.getPosition().getX(); // Use current position, not initial

            if (pVelocity > 0 && wallX - p.getRadius() > pCurrentPos) {
                return (wallX - p.getRadius() - pCurrentPos) / pVelocity;
            } else if (pVelocity < 0 && wallX + p.getRadius() < pCurrentPos) {
                return (wallX + p.getRadius() - pCurrentPos) / pVelocity;
            }

            return Double.POSITIVE_INFINITY;
        } else if (Math.abs(vertex1.getY() - vertex2.getY()) < 0.00001) { // horizontal wall
            final double wallY = vertex1.getY();

            pVelocity = p.getVelocity().getY();
            pCurrentPos = p.getPosition().getY(); // Use current position, not initial

            if (pVelocity > 0 && wallY - p.getRadius() > pCurrentPos) {
                return (wallY - p.getRadius() - pCurrentPos) / pVelocity;
            } else if (pVelocity < 0 && wallY + p.getRadius() < pCurrentPos) {
                return (wallY + p.getRadius() - pCurrentPos) / pVelocity;
            }

            return Double.POSITIVE_INFINITY;
        }

        return Double.POSITIVE_INFINITY;
    }

    @Override
    public String toString() {
        return String.format("%s %s", vertex1, vertex2);
    }
}
