package org.sims.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Wall {
    private final Orientation orientation;
    private final Vector vertex1;
    private final Vector vertex2;

    public Wall(final Orientation orientation, final Vector v1, final Vector v2) {
        this.orientation = orientation;
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

        walls.add(new Wall(Orientation.HORIZONTAL, new Vector(0, 0), new Vector(0.09, 0)));
        walls.add(new Wall(Orientation.VERTICAL, new Vector(0.09, 0), new Vector(0.09, lilCorner)));
        walls.add(new Wall(Orientation.HORIZONTAL, new Vector(0.09, lilCorner), new Vector(0.18, lilCorner)));
        walls.add(new Wall(Orientation.VERTICAL, new Vector(0.18, lilCorner), new Vector(0.18, lilCorner + L)));
        walls.add(new Wall(Orientation.HORIZONTAL, new Vector(0.18, lilCorner + L), new Vector(0.09, lilCorner + L)));
        walls.add(new Wall(Orientation.VERTICAL, new Vector(0.09, lilCorner + L), new Vector(0.09, 0.09)));
        walls.add(new Wall(Orientation.HORIZONTAL, new Vector(0.09, 0.09), new Vector(0, 0.09)));
        walls.add(new Wall(Orientation.VERTICAL, new Vector(0, 0.09), new Vector(0, 0)));

        return walls;
    }

    /**
     * Compute timo of the wall with the particle
     *
     * @apiNote This might look like magic, but it's just
     *          taking advantage of the fact that the logic is the
     *          same for vertical and horizontal walls, just
     *          swapping x and y
     *
     * @throws IllegalArgumentException if wall orientation is not VERTICAL or
     *                                  HORIZONTAL
     * @param p particle to check collision with
     * @return collision time
     */
    public double collisionTime(final Particle p) {
        if (orientation.equals(Orientation.ANY)) {
            throw new IllegalArgumentException("Unsupported Wall orientation");
        }

        // Get the constant coordinate of the wall (x for vertical, y for horizontal)
        final var wall = orientation.constant(vertex1);
        // Get the variable coordinates of the wall (y for vertical, x for horizontal)
        final var cvel = orientation.constant(p.getVelocity());
        final var cpos = orientation.constant(p.getPosition());

        // Check if the particle is moving towards the wall
        final var bottom_left = cpos < wall && cvel > 0;
        final var top_right = wall < cpos && cvel < 0;
        // These variables read as "the particle comes from a or b"

        if (!bottom_left && !top_right) {
            return Double.POSITIVE_INFINITY;
        }

        // If the particle comes from bottom/left, the radius is subtracted,
        // otherwise added
        final var r_effect = (bottom_left ? -1 : +1) * p.getRadius();
        final var time = (wall + r_effect - cpos) / cvel;

        // Never happens, top_left and bottom_right ensures the division is positive
        if (time < 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Get the future position of the particle at collision time
        final var future = Particle.move(p, time);

        // If the particle in the future is not between the wall's length
        if (!between(orientation.variable(future), orientation.variable(vertex1), orientation.variable(vertex2))) {
            return Double.POSITIVE_INFINITY;
        }

        // Finally, peace
        return time;
    }

    public static enum Orientation {
        VERTICAL(Vector::getX, Vector::getY), HORIZONTAL(Vector::getY, Vector::getX), ANY;

        private final Function<Vector, Double> constant;
        private final Function<Vector, Double> variable;

        Orientation(final Function<Vector, Double> constant, final Function<Vector, Double> variable) {
            this.constant = constant;
            this.variable = variable;
        }

        Orientation() {
            this(_ -> null, _ -> null);
        }

        /**
         * Get the constant coordinate of a vector according to the orientation type
         *
         * @param v vector to get coordinate from
         * @return constant value
         */
        public double constant(Vector v) {
            return constant.apply(v);
        }

        /**
         * Get the variable coordinate of a vector according to the orientation type
         *
         * @param v vector to get coordinate from
         * @return variable value
         */
        public double variable(Vector v) {
            return variable.apply(v);
        }
    }

    /**
     * Checks if a value is between two other values, inclusive,
     * regardless of order
     *
     * @param val value to check
     * @param a   first bound
     * @param b   second bound
     * @return true if val is between a and b
     */
    private static final boolean between(double val, double a, double b) {
        return Math.min(a, b) <= val && val <= Math.max(a, b);
    }

    @Override
    public String toString() {
        return String.format("%s %s", vertex1, vertex2);
    }
}
