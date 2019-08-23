package render.util;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL15.*;

/**
 * A {@link BakedMesh} which has an index buffer too (created automatically by the engine if necesssary)
 */
public class IndicesBakedMesh extends BakedMesh {

    protected int indicesVboId;

    IndicesBakedMesh(int vaoId, int vertexVboId, int indicesVboId, int vertexCount) {
        super(vaoId, vertexVboId, vertexCount);
        this.indicesVboId = indicesVboId;
    }

    @Override
    public void bind() {
        super.bind();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVboId);
    }

    @Override
    public void drawInBatch() {
        glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
    }

    @Override
    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
        super.unbind();
    }

    @Override
    public void free() {
        super.free();
        glDeleteBuffers(indicesVboId);
    }
}
