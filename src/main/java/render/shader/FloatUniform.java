package render.shader;

import org.lwjgl.opengl.GL20;

public class FloatUniform extends PrimitiveUniform<Float> {
    public FloatUniform(int location) {
        super(location);
    }

    @Override
    public void set(Float value) {
        GL20.glUniform1f(location, value);
    }
}
