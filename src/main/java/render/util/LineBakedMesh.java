package render.util;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glDrawArrays;

public class LineBakedMesh extends BakedMesh {
    LineBakedMesh(int vaoId, int vertexVboId, int vertexCount) {
        super(vaoId, vertexVboId, vertexCount);
    }

    @Override
    public void drawInBatch() {
        glDrawArrays(GL_LINES, 0, vertexCount);
    }

}
