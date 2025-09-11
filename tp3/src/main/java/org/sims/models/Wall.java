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

    public Vector getVertex1() {
        return vertex1;
    }
    public Vector getVertex2() {
        return vertex2;
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
    public double collisionTime(final Particle p) {
        final Vector pVelocity;
        final Vector pCurrentPos;

        if (Math.abs(vertex1.getX() - vertex2.getX()) < 0.00001) { // vertical wall
            final double wallX = vertex1.getX();

            pVelocity = p.getVelocity();
            pCurrentPos = p.getPosition(); // Use current position, not initial

            final double toReturn;

            if (wallX > pCurrentPos.getX() && pVelocity.getX() > 0) {
                toReturn = (wallX - p.getRadius() - pCurrentPos.getX()) / pVelocity.getX();

                if (toReturn < 0) {
                    return Double.POSITIVE_INFINITY;
                } else if(pCurrentPos.getY() + pVelocity.getY() * toReturn > Math.max(vertex1.getY(), vertex2.getY())
                        || pCurrentPos.getY() + pVelocity.getY() * toReturn < Math.min(vertex1.getY(), vertex2.getY())) {
                    
                    return toReturn;
                }
            } else if (wallX < pCurrentPos.getX() && pVelocity.getX() < 0) {
                toReturn = (wallX + p.getRadius() - pCurrentPos.getX()) / pVelocity.getX();

                if (toReturn < 0) {
                    return Double.POSITIVE_INFINITY;
                } else if(pCurrentPos.getY() + pVelocity.getY() * toReturn > Math.max(vertex1.getY(), vertex2.getY())
                        || pCurrentPos.getY() + pVelocity.getY() * toReturn < Math.min(vertex1.getY(), vertex2.getY())) {
                    
                    return toReturn;
                }
            }

            return Double.POSITIVE_INFINITY;
        } else if (Math.abs(vertex1.getY() - vertex2.getY()) < 0.00001) { // horizontal wall
            final double wallY = vertex1.getY();

            pVelocity = p.getVelocity();
            pCurrentPos = p.getPosition(); // Use current position, not initial

            if (wallY > pCurrentPos.getY() && pVelocity.getY() > 0) {
                final double toReturn = (wallY - p.getRadius() - pCurrentPos.getY()) / pVelocity.getY();

                if (toReturn < 0) {
                    return Double.POSITIVE_INFINITY;
                } else if(pCurrentPos.getX() + pVelocity.getX() * toReturn > Math.max(vertex1.getX(), vertex2.getX())
                        || pCurrentPos.getX() + pVelocity.getX() * toReturn < Math.min(vertex1.getX(), vertex2.getX())) {
                    
                    return toReturn;
                }
            } else if (wallY < pCurrentPos.getY() && pVelocity.getY() < 0) {
                final double toReturn = (wallY + p.getRadius() - pCurrentPos.getY()) / pVelocity.getY();

                if (toReturn < 0) {
                    return Double.POSITIVE_INFINITY;
                } else if(pCurrentPos.getX() + pVelocity.getX() * toReturn > Math.max(vertex1.getX(), vertex2.getX())
                        || pCurrentPos.getX() + pVelocity.getX() * toReturn < Math.min(vertex1.getX(), vertex2.getX())) {
                    
                    return toReturn;
                }
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
