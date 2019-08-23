package render.render3D.light;

import org.joml.Vector3f;


public abstract class PointLight {


    private Vector3f color;
    private float intensity;
    private Vector3f position;
    private Attenuation attenuation;


    public PointLight(Vector3f color, float intensity) {
        this.color = color;
        this.intensity = intensity;
        attenuation = new Attenuation(1, 0, 0);
        this.position = position;
    }

    public Vector3f getColor() {
        return color;
    }

    public void setColor(Vector3f color) {
        this.color = color;
    }

    public float getIntensity() {
        return intensity;
    }

    public void setIntensity(float intensity) {
        this.intensity = intensity;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    public Attenuation getAttenuation() {
        return attenuation;
    }

    public void setAttenuation(Attenuation attenuation) {
        this.attenuation = attenuation;
    }

}
