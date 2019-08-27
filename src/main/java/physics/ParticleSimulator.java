package physics;


import org.joml.Vector3f;
import util.Mathf;

import java.util.ArrayList;


public class ParticleSimulator {

    private ArrayList<Particle> particles = new ArrayList<>();
    private float g = -9.8f;
    private final float TPS = 60f;

    public ParticleSimulator() {

    }


    public void tick(){
        for(Particle p: particles) {
            p.velocity.add(0, g/TPS, 0);
            p.position.add(p.velocity.div(TPS, new Vector3f()));
        }

        cleanUp();
    }

    public void drawWorld() {
        for(Particle p: particles) {
            p.draw();
        }
    }



    public void addParticle(Particle particle) {
        particles.add(particle);
    }

    public Particle getParticle(int id) {
        if(particles.get(id) == null){
            throw new AssertionError("Particle does not exist");
        } else {
            return particles.get(id);
        }

    }

    private void cleanUp(){
        ArrayList<Particle> pToRemove = new ArrayList<>();
        for(Particle p: particles) {
            if(p.position.length() > 50) pToRemove.add(p);
        }
        for(Particle p: pToRemove) {
            particles.remove(p);
        }
    }


}
