package org.sims;

import org.sims.models.Particle;
import org.sims.models.Vector;

public class Main {
    public static void main(String[] args) {

        Particle p1 = new Particle(Vector.createNormalized(new Vector(1,1)),Vector.createNormalized(new Vector(1,1)), 1);
        Particle p2 = new Particle(new Vector(3,3),Vector.createNormalized(new Vector(-1,-1)), 1);
        Particle p3 = new Particle(new Vector(5,5),Vector.createNormalized(new Vector(1,1)), 1);
        Particle p4 = new Particle(new Vector(3,3),new Vector(0,0), 1);


        System.out.println("collision p1-p2: "+p1.collisionTime(p2));
        System.out.println("collision p2-p1: "+p2.collisionTime(p1));
        System.out.println("collision p1-p3 ===> should be inf: "+p1.collisionTime(p3));
        System.out.println("collision p1-p4 ===> should not be inf: "+p1.collisionTime(p4));
    }
}