package render.util;

import static org.lwjgl.opengl.GL11.*;

public class LineIndicesBakedMesh extends IndicesBakedMesh {
    public LineIndicesBakedMesh(int vaoId, int vertexVboId, int indicesVboId, int vertexCount) {
        super(vaoId, vertexVboId, indicesVboId, vertexCount);
    }

    @Override
    public void drawInBatch() {
        glDrawElements(GL_LINES, vertexCount, GL_UNSIGNED_INT, 0);
    }
}
