package org.sims;

import org.sims.models.Particle;
import org.sims.models.Vector;
import org.sims.models.Wall;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {

        Particle p1 = new Particle(Vector.createNormalized(new Vector(1,1)),Vector.createNormalized(new Vector(1,1)), 1);
        Particle p2 = new Particle(new Vector(3,3),Vector.createNormalized(new Vector(-1,-1)), 1);
        Particle p3 = new Particle(new Vector(5,5),Vector.createNormalized(new Vector(1,1)), 1);
        Particle p4 = new Particle(new Vector(3,3),new Vector(0,0), 1);

        Wall vWall1 = new Wall(new Vector(0,0), new Vector(1,0));
        Wall hWall1 = new Wall(new Vector(1,0), new Vector(1,1));
        Wall vWall2 = new Wall(new Vector(1,1), new Vector(0,1));
        Wall hWall2 = new Wall(new Vector(0,1), new Vector(0,0));

        List<Wall> walls = new ArrayList<>(List.of(vWall1, vWall2, hWall1, hWall2));
        System.out.println("collision p1-vWall1: "+ p1.collisionTimeWithWalls(walls));


//        System.out.println("collision p1-p2: "+p1.collisionTime(p2));
//        System.out.println("collision p2-p1: "+p2.collisionTime(p1));
//        System.out.println("collision p1-p3 ===> should be inf: "+p1.collisionTime(p3));
//        System.out.println("collision p1-p4 ===> should not be inf: "+p1.collisionTime(p4));
    }

    //TODO wall generator that checks closed polygon


}