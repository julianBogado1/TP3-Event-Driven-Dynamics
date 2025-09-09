package org.sims;

import org.sims.models.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;

public class Main {
    private static final int ANIMATION_INTERVAL = 5;

    public static void main(String[] args) throws Exception {
        final var walls = Wall.generate(0.07);
        final var particles = Particle.generateInitialState(20, 0.007, 0.0015);

        try (final var writer = Resources.writer("setup.txt")) {
            writer.write("%d %.14f\n".formatted(particles.size(), 0.06));
            for (final var w : walls) {
                writer.write("%s\n".formatted(w));
            }
        }

        Resources.preparePath("steps");

        System.out.println("Starting simulation...");
        System.out.println(particles);
        System.out.println(walls);
        Simulator sim = new Simulator(5000, particles, walls);
        sim.simulate();
        System.out.println("Simulation complete.");

    }
    public static class Simulator{
        private final int steps;
        private List<Particle> particles;
        private List<Wall> walls;


        public Simulator(int steps, List<Particle> particles, List<Wall> walls) {
            this.steps = steps;
            this.walls = walls;
            this.particles = particles;
        }

        public void simulate() throws IOException {
            Event event;
            for(int i=0; i<steps; i++){
                try (BufferedWriter writer = Resources.writer("steps", "%d.txt".formatted(i))) {
                    for (Particle p : particles) {
                        writer.write(p.toString() + "\n");
                    }
                }
                event = nextEvent(particles, walls);
                double dt = event.collisionTime;
                for(Particle p: particles){
                    p.move(dt);
                }
                event.execute();

            }

        }


        /**
         * Checksthe next event to happen in the system
         *
         * @param particles List of particles in the system
         * @param walls boundaries of the system
         * @return The next event to happen
         */
        private Event nextEvent(List<Particle> particles, List<Wall> walls){
            Event particleCollision = new ParticleCollision(null, null, Double.POSITIVE_INFINITY);
            Event wallCollision = new WallCollision(null, null, Double.POSITIVE_INFINITY);
            for(Particle p : particles){
                for(Particle other:particles){
                    Event temp = p.collisionTime(other);
                    if(temp.collisionTime< particleCollision.collisionTime){particleCollision = temp;}
                }
                for(Wall w : walls){
                    Event temp = p.collisionTime(w);
                    if(temp.collisionTime< wallCollision.collisionTime){wallCollision = temp;}
                }
            }
            return (particleCollision.collisionTime < wallCollision.collisionTime) ? particleCollision : wallCollision;
        }

    }
}
