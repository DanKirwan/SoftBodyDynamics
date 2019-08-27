package physics;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.CallbackI;

import static render.shader.Shaders.sceneShader;

public class Particle {
    public float mass;
    public Vector3f position;
    public Vector3f velocity;

    public Mesh mesh;
    public Matrix4f modelMat;

    public Particle(Vector3f pos, Vector3f vel, float mass, Mesh mesh) {
        position = pos;
        velocity = vel;
        this.mass = mass;
        this.mesh = mesh;
        modelMat = new Matrix4f().scale(mass);

    }

    public Particle(Mesh mesh) {
        position = new Vector3f();
        velocity = new Vector3f();
        mass = 1f;
        this.mesh = mesh;
        modelMat = new Matrix4f().scale(mass);
    }

    public void draw(){
        //doesnt bind shader
        modelMat.setTranslation(position);
        sceneShader.modelMat.set(modelMat);
        mesh.getMesh().bind();
        mesh.getMesh().draw();
    }

    public Particle randVelocity() {
        velocity.set( -10f + (float) Math.random() * 20f, (float) Math.random() * 20f, -10f + (float) Math.random() * 20f);
        return this;
    }

    public Particle randPosition(float radius) {
        position.set((float) Math.random() * radius,
                (float) Math.random() * radius,
                (float) Math.random() * radius);
        return this;
    }

    public Particle randMass(float maxMass) {
        mass = (float) Math.random() * maxMass;
        modelMat.identity().scale(mass);
        return this;
    }



}
