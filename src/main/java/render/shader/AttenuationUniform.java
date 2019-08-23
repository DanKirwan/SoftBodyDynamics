package render.shader;

import render.render3D.light.Attenuation;

public class AttenuationUniform extends Uniform<Attenuation> {
    private final Uniform<Float> constant;
    private final Uniform<Float> linear;
    private final Uniform<Float> exponent;

    public AttenuationUniform(Uniform<Float> constant, Uniform<Float> linear, Uniform<Float> exponent) {
        this.constant = constant;
        this.linear = linear;
        this.exponent = exponent;
    }

    @Override
    public void set(Attenuation value) {
        constant.set(value.getConstant());
        linear.set(value.getLinear());
        exponent.set(value.getExponent());
    }
}
