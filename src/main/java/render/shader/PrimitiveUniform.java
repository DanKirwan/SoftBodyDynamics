package render.shader;

public abstract class PrimitiveUniform<T> extends Uniform<T> {

    protected int location;

    public PrimitiveUniform(int location) {
        this.location = location;
    }

}
