package org.sims.models;

public record Vector(double x, double y) {
    public static final Vector NONE_NONE = new Vector(-1.0, -1.0);
    public static final Vector NONE_ZERO = new Vector(-1.0, 0.0);
    public static final Vector NONE_ONE = new Vector(-1.0, 1.0);

    public static final Vector ZERO_NONE = new Vector(0.0, -1.0);
    public static final Vector ZERO_ZERO = new Vector(0.0, 0.0);
    public static final Vector ZERO_ONE = new Vector(0.0, 1.0);

    public static final Vector ONE_NONE = new Vector(1.0, -1.0);
    public static final Vector ONE_ZERO = new Vector(1.0, 0.0);
    public static final Vector ONE_ONE = new Vector(1.0, 1.0);

    public static final Vector ZERO = ZERO_ZERO;

    public static Vector createNormalized(Vector v) {
        final var norm = norm(v);
        return new Vector(v.x / norm, v.y / norm);
    }

    public Vector neg() {
        return Vector.neg(this);
    }

    public Vector add(Vector v) {
        return Vector.add(this, v);
    }

    public Vector subtract(Vector v) {
        return Vector.subtract(this, v);
    }

    public Vector mult(double scalar) {
        return Vector.mult(this, scalar);
    }

    public Vector div(double scalar) {
        return Vector.div(this, scalar);
    }

    public double dot(Vector v) {
        return Vector.dot(this, v);
    }

    public double norm() {
        return Math.sqrt(x * x + y * y);
    }

    public double angle(Vector v) {
        return Vector.angle(this, v);
    }

    public Vector hadamard(Vector v) {
        return Vector.hadamard(this, v);
    }

    public static Vector neg(Vector v) {
        return new Vector(-v.x, -v.y);
    }

    public static Vector add(Vector v1, Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y);
    }

    public static Vector subtract(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }

    public static Vector mult(Vector v, double scalar) {
        return new Vector(v.x * scalar, v.y * scalar);
    }

    public static Vector div(Vector v, double scalar) {
        return new Vector(v.x / scalar, v.y / scalar);
    }

    public static double dot(Vector v1, Vector v2) {
        return v1.x * v2.x + v1.y * v2.y;
    }

    public static double norm(Vector v) {
        return Math.sqrt(v.x * v.x + v.y * v.y);
    }

    /**
     * Computes angle between 2 vectors
     * @param v1 first vector
     * @param v2 second vector
     * @return angle in radians
     */
    public static double angle(Vector v1, Vector v2){
        double norm1 = norm(v1);
        double norm2 = norm(v2);
        return Math.acos(v1.dot(v2)/ (norm1*norm2));
    }

    public static Vector hadamard(Vector v1, Vector v2) {
        return new Vector(v1.x * v2.x, v1.y * v2.y);
    }

    /**
     * Returns this vector as a column matrix
     * @return this vector as a vector of 1 vector
     */
    public double[][] toColumnMatrix(){
        return new double[][]{{x},
                             {y}};
    }

    @Override
    public String toString() {
        return String.format("%.14f %.14f", x, y);
    }
}
