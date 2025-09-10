package org.sims;

import com.google.gson.Gson;
import org.sims.models.*;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {
    private static final int ANIMATION_INTERVAL = 5;

    public static void main(String[] args) throws Exception {

        //============ Read config file =============//
        Gson gson = new Gson();
        Map<String, Number> config = gson.fromJson(new FileReader("src/main/resources/config.json"), Map.class);
        double radius, velocity, L;
        int N, steps;
        radius = config.get("radius").doubleValue();
        velocity = config.get("velocity").doubleValue();
        L = config.get("L").doubleValue();
        N = config.get("particles").intValue();
        steps = config.get("steps").intValue();


        //============ Initial State =============//
        final var walls = Wall.generate(L);
        final var particles = Particle.generateInitialState(N, velocity, radius);

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
        Simulator sim = new Simulator(steps, particles, walls);
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
                System.out.println("Step: "+i+" Time to next event: "+dt);
                for(Particle p: particles){
                    System.out.println("Particle: "+p);
                    p.move(dt);
                    System.out.println("Moved Particle: "+p);
                }
                event.execute();
                System.out.println("Executed event");
                System.out.println("Collisioned particle: "+particles);

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
//            Event particleCollision = new ParticleCollision(null, null, Double.POSITIVE_INFINITY);
            Event wallCollision = new WallCollision(null, null, Double.POSITIVE_INFINITY);
            for(Particle p : particles){
//                for(Particle other:particles){
//                    Event temp = p.collisionTime(other);
//                    if(temp.collisionTime< particleCollision.collisionTime){particleCollision = temp;}
//                }
                for(Wall w : walls){
                    Event temp = p.collisionTime(w);
                    if(temp.collisionTime< wallCollision.collisionTime){wallCollision = temp;}
                }
            }
//            Event tc =  (particleCollision.collisionTime < wallCollision.collisionTime) ? particleCollision : wallCollision;
            WallCollision tc = (WallCollision) wallCollision;
//            System.out.println("Collision time: "+tc.collisionTime);
//            System.out.println("##Particle: "+tc.getParticle()+" Wall: "+tc.getWall());
            return tc;
        }

    }
}
