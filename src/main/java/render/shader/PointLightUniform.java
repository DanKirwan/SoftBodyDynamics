package render.shader;

import render.render3D.light.Attenuation;
import org.joml.Vector3f;
import render.render3D.light.PointLight;

public class PointLightUniform extends Uniform<PointLight> {
    private final Uniform<Vector3f> color;
    private final Uniform<Vector3f> position;
    private final Uniform<Float> intensity;
    private final Uniform<Attenuation> attenuation;

    public PointLightUniform(Uniform<Vector3f> color, Uniform<Vector3f> position, Uniform<Float> intensity, Uniform<Attenuation> attenuation) {
        this.color = color;
        this.position = position;
        this.intensity = intensity;
        this.attenuation = attenuation;
    }

    @Override
    public void set(PointLight value) {
        color.set(value.getColor());
        position.set(value.getPosition());
        intensity.set(value.getIntensity());
        attenuation.set(value.getAttenuation());
    }
}
