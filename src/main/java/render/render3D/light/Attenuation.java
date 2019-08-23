package render.render3D.light;

/**
 * A class to model the effects of Attenuation - the further from a light source you are the lower the intensity
 *
 * This is defined by the equation: 1.0/(Constant+Linear∗distance+Exponent∗dist^2)
 */

public class Attenuation {

    private float constant;

    private float linear;

    private float exponent;

    private Attenuation() {}

    public Attenuation(float constant, float linear, float exponent) {
        this.constant = constant;
        this.linear = linear;
        this.exponent = exponent;
    }

    public Attenuation(Attenuation attenuation){
        this(attenuation.getConstant(),attenuation.getLinear(),attenuation.getExponent());
    }

    public float getConstant() {
        return constant;
    }

    public void setConstant(float constant) {
        this.constant = constant;
    }

    public float getLinear() {
        return linear;
    }

    public void setLinear(float linear) {
        this.linear = linear;
    }

    public float getExponent() {
        return exponent;
    }

    public void setExponent(float exponent) {
        this.exponent = exponent;
    }
}