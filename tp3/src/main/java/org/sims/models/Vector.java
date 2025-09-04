package org.sims.models;

public class Vector {
    public double x;
    public double y;
    public Vector(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public static Vector createNormalized(Vector v){
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
    public void addInPlace(Vector v) {
        this.x += v.x;
        this.y += v.y;
    }
    public double norm(){
        return Math.sqrt(x*x + y*y);
    }

    public static Vector add(Vector v1, Vector v2){
        return new Vector(v1.x + v2.x, v1.y + v2.y);
    }

    public static Vector subtract(Vector v1, Vector v2){
        return new Vector(v1.x - v2.x, v1.y - v2.y);
    }

    public double dot(Vector v){
        return this.x * v.x + this.y * v.y;
    }

    public static double norm(Vector v){
        return Math.sqrt(v.x*v.x + v.y*v.y);
    }public static double norm(double x, double y){
        return Math.sqrt(x*x + y*y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vector vector = (Vector) o;
        return Double.compare(vector.x, x) == 0 && Double.compare(vector.y, y) == 0;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", x, y);
    }
}
