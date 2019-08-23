package render.shader;

import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;

public class Vector4fUniform extends PrimitiveUniform<Vector4f> {
    public Vector4fUniform(int location) {
        super(location);
    }

    @Override
    public void set(Vector4f value) {
        GL20.glUniform4f(location, value.x, value.y, value.z, value.w);
    }
}
