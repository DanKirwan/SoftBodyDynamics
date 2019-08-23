package render.shader;

import org.joml.Matrix3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

public class Matrix3fUniform extends PrimitiveUniform<Matrix3f> {
    public Matrix3fUniform(int location) {
        super(location);
    }

    @Override
    public void set(Matrix3f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buf = stack.mallocFloat(9);
            value.get(buf);
            GL20.glUniformMatrix3fv(location, false, buf);
        }
    }
}
