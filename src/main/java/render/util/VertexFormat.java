package render.util;

import render.shader.ShaderProgram;

import java.nio.ByteBuffer;

import static render.util.VertexAttribute.*;
import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Specifies which attributes of a vertex will be specified to the shader. These are essentially lists of
 * {@link VertexAttribute}. It is specified when building a {@link BakedMesh}.
 *
 * @see VertexAttribute
 * @see MeshBuilder
 */
public class VertexFormat {


    public static final VertexFormat POS2_COL = new VertexFormat(POSITION2_FLOAT, COLOR_FLOAT);

    public static final VertexFormat POS2_COL4 = new VertexFormat(POSITION2_FLOAT, COLOR4_FLOAT);

    public static final VertexFormat POS2_TEX = new VertexFormat(POSITION2_FLOAT, TEXTURE_FLOAT);

    public static final VertexFormat POS2_TEX_COL4 = new VertexFormat(POSITION2_FLOAT, TEXTURE_FLOAT, COLOR4_FLOAT);

    public static final VertexFormat POS2 = new  VertexFormat(POSITION2_FLOAT);



    public static final VertexFormat POS = new VertexFormat(POSITION_DOUBLE);

    public static final VertexFormat POS_NORM = new VertexFormat(POSITION_DOUBLE, NORMAL_FLOAT);

    public static final VertexFormat POS_COL_NORM = new VertexFormat(POSITION_DOUBLE, COLOR_FLOAT, NORMAL_FLOAT);

    public static final VertexFormat POS_COL4_NORM = new VertexFormat(POSITION_DOUBLE, COLOR4_FLOAT, NORMAL_FLOAT);

    public static final VertexFormat POS_TEX_NORM = new VertexFormat(POSITION_DOUBLE, TEXTURE_FLOAT, NORMAL_FLOAT);

    public static final VertexFormat POS_TEX_NORM_TANG_BITANG = new VertexFormat(POSITION_FLOAT, TEXTURE_FLOAT, NORMAL_FLOAT, TANGENT_FLOAT, BITANGENT_FLOAT);
    //only use for checking all supported vertex types

    public static final VertexFormat POS_TEX_COL4_NORM_TANG_BITANG = new VertexFormat(POSITION_FLOAT, TEXTURE_FLOAT, COLOR4_FLOAT, NORMAL_FLOAT, TANGENT_FLOAT, BITANGENT_FLOAT);

    private VertexAttribute[] attribs;
    /**
     * The number of bytes per vertex
     */
    private int vertexSize;

    public VertexFormat(VertexAttribute... attribs) {
        this.attribs = attribs;

        this.vertexSize = 0;
        for (VertexAttribute attrib : attribs)
            this.vertexSize += attrib.getSize();
    }

    /**
     * An array of all the attributes of this vertex format
     */
    public VertexAttribute[] getAttributes() {
        return attribs;
    }

    /**
     * The number of bytes per vertex
     */
    public int getVertexSize() {
        return vertexSize;
    }

    /**
     * Returns whether this vertex format is <i>compatible</i> with the other one.
     *
     * <p>
     * Two vertex formats are compatible if and only if each can be substituted for
     * the other in {@link MeshBuilder} such that if the subsequent calls to the mesh
     * builder were valid before, they won't be invalid after.
     * </p><p>
     * In other words, two vertex formats <tt>a</tt> and <tt>b</tt> are compatible if
     * and only if they have the same number of attributes, and each attribute in
     * <tt>a</tt> is of the same type as the corresponding attribute in <tt>b</tt>.
     * </p>
     *
     * @see VertexAttribute#getType()
     * @see #isLenientlyCompatible(VertexFormat)
     */
    public boolean isCompatible(VertexFormat other) {
        if (attribs.length != other.attribs.length)
            return false;

        for (int i = 0; i < attribs.length; i++) {
            if (attribs[i].getType() != other.attribs[i].getType())
                return false;
        }

        return true;
    }

    /**
     * Returns whether this parent vertex format is <i>leniently compatible</i> with the
     * given child vertex format. Note that unlike compatibility, this is a one-way
     * relationship, and the parent being leniently compatible with the child does not
     * imply that the child is leniently compatible with the parent.
     *
     * <p>
     * A parent vertex format is leniently compatible with a child vertex format if and only
     * if the parent can be substituted for the child in {@link MeshBuilder}, such that if
     * the subsequent calls to the mesh builder were valid before, if lenient mode is set
     * to true the calls won't be invalid after.
     * </p><p>
     * In other words, the parent is leniently compatible with the child if and only if a
     * subsequence of the parent's attributes is compatible with the child.
     * </p>
     *
     * @see #isCompatible(VertexFormat)
     * @see MeshBuilder#setLenient(boolean)
     */
    public boolean isLenientlyCompatible(VertexFormat child) {
        int childIndex = 0;
        for (VertexAttribute parentAttrib : attribs) {
            if (childIndex < child.attribs.length && parentAttrib.getType() == child.attribs[childIndex].getType()) {
                childIndex++;
            }
        }
        return childIndex == child.attribs.length;
    }

    /**
     * Returns whether this vertex format is compatible with the currently bound shader
     *
     * @see #isCompatible(VertexFormat)
     */
    public boolean isCurrentlyCompatible() {
        ShaderProgram boundShader = ShaderProgram.getBoundShader();
        return boundShader != null && isCompatible(boundShader.getVertexFormat());
    }

    /**
     * Returns whether this vertex format is leniently compatible with the currently bound shader
     *
     * @see #isLenientlyCompatible(VertexFormat)
     */
    public boolean isCurrentlyLenientlyCompatible() {
        ShaderProgram boundShader = ShaderProgram.getBoundShader();
        return boundShader != null && isLenientlyCompatible(boundShader.getVertexFormat());
    }

    public boolean hasAttributeType(VertexAttribute.Type attrib) {
        for(VertexAttribute att : attribs) {
            if(att.getType() == attrib) {
                return true;
            }
        }
        return false;
    }

    /**
     * Does the GL calls to create a {@link BakedMesh}. This takes the raw buffer and its length
     * and creates a mesh. This is really an internal method, you should use {@link MeshBuilder#createMesh()},
     * which calls this, instead.
     *
     * @see MeshBuilder
     */
    public BakedMesh createMesh(ByteBuffer vertexBuffer, int vertexBufferSize, ByteBuffer indicesBuffer, int indicesCount, int primitiveDrawType) {
        int vaoId = glGenVertexArrays();
        glBindVertexArray(vaoId); // so operations apply to this VAO

        for (int i = 0; i < attribs.length; i++)
            glEnableVertexAttribArray(i);

        int vertexVboId = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexVboId); // so operations apply to this VBO
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        int ptr = 0;
        for (int i = 0; i < attribs.length; i++) {
            glVertexAttribPointer(i, attribs[i].getCount(), attribs[i].getDataType(), false, vertexSize, ptr);
            ptr += attribs[i].getSize();
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        int indicesVboId = 0;
        if (indicesCount > 0) {
            indicesVboId = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, indicesVboId);
            glBufferData(GL_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
            glBindBuffer(GL_ARRAY_BUFFER, 0);
        }

        glBindVertexArray(0);

        for (int i = 0; i < attribs.length; i++)
            glDisableVertexAttribArray(i);

        if(primitiveDrawType == GL_TRIANGLES) {
            if (indicesVboId != 0) {
                return new IndicesBakedMesh(vaoId, vertexVboId, indicesVboId, indicesCount);
            } else {
                return new BakedMesh(vaoId, vertexVboId, vertexBufferSize / vertexSize);
            }
        } else if(primitiveDrawType == GL_LINES) {
            if (indicesVboId != 0) {
                return new LineIndicesBakedMesh(vaoId, vertexVboId, indicesVboId, indicesCount);
            } else {
                return new LineBakedMesh(vaoId, vertexVboId, vertexBufferSize / vertexSize);
            }

        } else {
            throw new IllegalArgumentException("The Primitive Draw type must be either GL_TRIANGLES or GL_LINES");
        }
    }

}
