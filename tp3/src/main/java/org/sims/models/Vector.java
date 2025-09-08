package org.sims.models;

import java.util.Objects;

public class Vector {
    private double x;
    private double y;

    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public static Vector createNormalized(Vector v) {
        double norm = norm(v);
        return new Vector(v.x / norm, v.y / norm);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public Vector add(Vector v) {
        return new Vector(this.x + v.x, this.y + v.y);
    }

    public Vector mult(double scalar) {
        return new Vector(this.x * scalar, this.y * scalar);
    }

    public void addInPlace(Vector v) {
        this.x += v.x;
        this.y += v.y;
    }

    public double norm() {
        return Math.sqrt(x * x + y * y);
    }

    public static Vector add(Vector v1, Vector v2) {
        return new Vector(v1.x + v2.x, v1.y + v2.y);
    }

    public static Vector subtract(Vector v1, Vector v2) {
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }

    public double dot(Vector v) {
        return this.x * v.x + this.y * v.y;
    }

    public static double norm(Vector v) {
        return Math.sqrt(v.x * v.x + v.y * v.y);
    }

    public static double norm(double x, double y) {
        return Math.sqrt(x * x + y * y);
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

    /**
     * Returns this vector as a column matrix
     * @return this vector as a vector of 1 vector
     */
    public double[][] toColumnMatrix(){
        return new double[][]{{x},
                             {y}};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || !(o instanceof Vector other))
            return false;

        return Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return String.format("%.14f %.14f", x, y);
    }
}
