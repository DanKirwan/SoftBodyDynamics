package render.shader;

import org.lwjgl.opengl.GL20;

public class BooleanUniform extends PrimitiveUniform<Boolean> {
    public BooleanUniform(int location) {
        super(location);
    }

    @Override
    public void set(Boolean value) {
        GL20.glUniform1i(location, value ? 1 : 0);
    }
}
