package org.sims;

import org.sims.models.Particle;
import org.sims.models.Wall;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        List<Wall> walls = Wall.generate(0.06);
        List<Particle> particles = Particle.generateInitialState(20, 0.3,0.005 );

        BufferedWriter writer = new BufferedWriter(new FileWriter("src/main/resources/particles.txt"));
        writer.write("Walls:\n");
        for(Wall w: walls){
            writer.write(w.toString()+"\n");
        }
        writer.write("Particles:\n");
        for (Particle p : particles) {
            writer.write(p.toString()+"\n");
        }
        writer.close();
    }

    //TODO wall generator that checks closed polygon


}