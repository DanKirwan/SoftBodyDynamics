package render.shader;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class Matrix4fUniform extends PrimitiveUniform<Matrix4f> {
    public Matrix4fUniform(int location) {
        super(location);
    }

    @Override
    public void set(Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(16);
            value.get(buf);
            GL20.glUniformMatrix4fv(location, false, buf);
        }
    }
}
