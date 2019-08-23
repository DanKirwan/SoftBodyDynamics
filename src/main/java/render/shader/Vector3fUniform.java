package render.shader;

import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;

public class Vector3fUniform extends PrimitiveUniform<Vector3f> {
    public Vector3fUniform(int location) {
        super(location);
    }

    @Override
    public void set(Vector3f value) {
        GL20.glUniform3f(location, value.x, value.y, value.z);
    }
}
