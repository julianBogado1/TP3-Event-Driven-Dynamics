package org.sims;

import org.sims.models.Particle;
import org.sims.models.Vector;
import org.sims.models.Wall;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        final var walls = Wall.generate(0.06);
        final var particles = Particle.generateInitialState(20, 0.3, 0.005);
        // Test cases
        runTest(
                new Particle(new Vector(0, 0), new Vector(1, 0), 1),
                new Particle(new Vector(1, 0), new Vector(-1, 0), 1),
                "Head-on collision along X axis"
        );

        runTest(
                new Particle(new Vector(0, 0), new Vector(0, 1), 1),
                new Particle(new Vector(0, 1), new Vector(0, -1), 1),
                "Head-on collision along Y axis"
        );

        runTest(
                new Particle(new Vector(0, 0), new Vector(1, 1), 1),
                new Particle(new Vector(1, 1), new Vector(-1, -1), 1),
                "Diagonal collision"
        );

        runTest(
                new Particle(new Vector(0, 0), new Vector(1, 0), 1),
                new Particle(new Vector(0, 1), new Vector(0, -1), 1),
                "Orthogonal velocities"
        );
    }

    private static void runTest(Particle p1, Particle p2, String description) {
        System.out.println("==== " + description + " ====");
        System.out.println("Before:");
        System.out.println("P1 velocity = " + p1.getVelocity());
        System.out.println("P2 velocity = " + p2.getVelocity());

        Particle.collide(p1, p2);

        System.out.println("After:");
        System.out.println("P1 velocity = " + p1.getVelocity());
        System.out.println("P2 velocity = " + p2.getVelocity());
        System.out.println();
    }

    // TODO wall generator that checks closed polygon
}
