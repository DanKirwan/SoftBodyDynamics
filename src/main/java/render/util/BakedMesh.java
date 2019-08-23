package render.util;

import render.shader.ShaderProgram;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;

/**
 * Stores vertices, along with all their attributes, ready to be drawn. (As raw GL IDs).
 *
 * <p>
 * Creating a BakedMesh is done via a builder, typically like this:
 *
 * <blockquote><pre>
 *     BakedMesh triangle = BakedMesh.builder(VertexFormat.POS2_COL)
 *             .pos(-1, -1).col(0f, 1f, 1f)
 *             .pos(0, 1).col(1f, 0f, 1f)
 *             .pos(1, -1).col(1f, 1f, 0f)
 *             .createMesh();
 * </pre></blockquote>
 * </p>
 *
 * @see #builder(VertexFormat)
 * @see MeshBuilder
 */
public class BakedMesh {

    private static BakedMesh boundMesh = null;
    private static long nextId;
    protected long id;
    protected int vaoId;
    protected int vertexVboId;
    protected int vertexCount;

    BakedMesh(int vaoId, int vertexVboId, int vertexCount) {
        this.id = nextId++;
        this.vaoId = vaoId;
        this.vertexVboId = vertexVboId;
        this.vertexCount = vertexCount;
    }

    /**
     * Creates a {@link MeshBuilder} with the given {@link VertexFormat}.
     */
    public static MeshBuilder builder(VertexFormat vertexFormat) {
        return new MeshBuilder(vertexFormat);
    }

    public static MeshBuilder builder(VertexFormat vertexFormat, int primitiveDrawType){
        return new MeshBuilder(vertexFormat, primitiveDrawType);
    }

    public long getId() {
        return id;
    }

    /**
     * Draws this mesh. Does not bind the shader, remember to do that yourself with {@link ShaderProgram#bind()}.
     */
    public final void draw() {
        bind();
        drawInBatch();
        unbind();
    }

    public void bind() {
        boundMesh = this;
        glBindVertexArray(vaoId);
    }

    public void drawInBatch() {
        glDrawArrays(GL_TRIANGLES, 0, vertexCount);
    }

    public void unbind() {
        glBindVertexArray(0);
        boundMesh = null;
    }

    public static BakedMesh getBoundMesh() {
        return boundMesh;
    }

    /**
     * Frees this BakedMesh and all the resources associated with it.
     */
    public void free() {
        glDeleteVertexArrays(vaoId);
        glDeleteBuffers(vertexVboId);
    }

}
