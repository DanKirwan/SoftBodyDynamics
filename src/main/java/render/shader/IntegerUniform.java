package render.shader;

import org.lwjgl.opengl.GL20;

public class IntegerUniform extends PrimitiveUniform<Integer> {

    public IntegerUniform(int location) {
        super(location);
    }

    @Override
    public void set(Integer value) {
        GL20.glUniform1i(location, value);
    }
}
