package render.util;

import static org.lwjgl.opengl.GL11.*;
import static render.util.VertexAttribute.Type.*;

/**
 * Specifies a vertex attribute, which is any information you want attached to a vertex.
 * You rarely use this class directly, multiple attributes are generally compounded into
 * a {@link VertexFormat}.
 *
 * <p>
 * At the frontend, generally you are only concerned with the {@link VertexAttribute.Type},
 * and are not bothered by exactly how that data will be stored. Which exact POSITION
 * you choose for example depends on how the shader accepts vertex data, and the
 * engine should convert the data you give it with {@link MeshBuilder} into this format
 * automatically.
 * </p>
 *
 * @see VertexFormat
 */
public enum VertexAttribute {

    // NULL ATTRIBUTE

    /**
     * Used mostly internally as a vertex attribute nothing is compatible with
     */
    NULL(Type.NULL, GL_BYTE, 0),


    // POSITION ATTRIBUTES

    /**
     * 3 ints representing the position of the vertex
     */
    POSITION_INTEGER(POSITION, GL_INT, 3),
    /**
     * 3 floats representing the position of the vertex
     */
    POSITION_FLOAT(POSITION, GL_FLOAT, 3),
    /**
     * 3 doubles representing the position of the vertex
     */
    POSITION_DOUBLE(POSITION, GL_DOUBLE, 3),
    /**
     * 2 ints representing the 2d position of the vertex
     */
    POSITION2_INTEGER(POSITION, GL_INT, 2),
    /**
     * 2 floats representing the 2d position of the vertex
     */
    POSITION2_FLOAT(POSITION, GL_FLOAT, 2),
    /**
     * 2 doubles representing the 2d position of the vertex
     */
    POSITION2_DOUBLE(POSITION, GL_DOUBLE, 2),


    // COLOR ATTRIBUTES

    /**
     * 3 floats representing the RGB color of the vertex
     */
    COLOR_FLOAT(COLOR, GL_FLOAT, 3),
    /**
     * 4 floats representing the RGBA color of the vertex
     */
    COLOR4_FLOAT(COLOR, GL_FLOAT, 4),


    // TEXTURE COORDINATE ATTRIBUTES

    /**
     * 2 ints representing the UV texture coordinates of the vertex
     */
    TEXTURE_INTEGER(TEXTURE, GL_INT, 2),
    /**
     * 2 floats representing the UV texture coordinates of the vertex
     */
    TEXTURE_FLOAT(TEXTURE, GL_FLOAT, 2),
    /**
     * 2 doubles representing the UV texture coordinates of the vertex
     */
    TEXTURE_DOUBLE(TEXTURE, GL_DOUBLE, 2),


    // NORMAL ATTRIBUTES

    /**
     * 3 floats representing the normal vector at a vertex
     */
    NORMAL_FLOAT(NORMAL, GL_FLOAT, 3),
    /**
     * 3 doubles representing the normal vector at a vertex
     */
    NORMAL_DOUBLE(NORMAL, GL_DOUBLE, 3),


    //TANGENT ATTRIBUTES

    /**
     * 3 floats representing the tangent vector at a vertex
     */
    TANGENT_FLOAT(TANGENT, GL_FLOAT, 3),


    //BITANGENT ATTRIBUTES

    /**
     * 3 floats representing the bitangent vector at a vertex (used for normal mapping)
     */
    BITANGENT_FLOAT(BITANGENT, GL_FLOAT, 3)


    ;

    private final Type type;
    private final int dataType;
    private final int count;
    private final int size;

    VertexAttribute(Type type, int dataType, int count) {
        this.type = type;
        this.dataType = dataType;
        this.count = count;
        this.size = count * OpenGlUtil.sizeof(dataType);
    }

    /**
     * The type of vertex attribute. Attributes of the same type can be implicitly be converted
     */
    public Type getType() {
        return type;
    }

    /**
     * The dataType of the values of the attribute
     */
    public int getDataType() {
        return dataType;
    }

    /**
     * The number of values in the attribute
     */
    public int getCount() {
        return count;
    }

    /**
     * The number of bytes the attribute takes up for one vertex
     */
    public int getSize() {
        return size;
    }

    /**
     * Specifies the type of vertex attribute, which is what you're concerned with at the frontend
     */
    public static enum Type {
        NULL, POSITION, COLOR, TEXTURE, NORMAL, TANGENT, BITANGENT
    }
}
