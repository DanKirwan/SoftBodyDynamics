package render.util;

import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * <p>
 * The builder class for {@link BakedMesh}. You use this builder to add one vertex attribute at a time.
 * See the documentation for <tt>BakedMesh</tt> for an example usage of this class. It is recommended to
 * format the code with one vertex per line, and multiple vertex attributes on the same line, as in
 * that example.
 * </p><p>
 * The methods of this class follow the builder pattern. That is, the methods return <tt>this</tt>
 * such that more methods can be called on the return value, allowing for the call chaining seen
 * in the example. Once you have finished adding all the vertices, use the {@link #createMesh()}
 * method, which returns an instance of <tt>BakedMesh</tt>.
 * </p><p>
 * This class ensures you are inserting the right data into the internal buffer correctly, throwing
 * an exception if you don't. It also hides away the details of the shader, the VAO and the VBO.
 * The class makes some attempt to convert your input data to the correct data. For example it
 * can convert a 2D coordinate to a 3D coordinate with a z-position of 0. However, trying to
 * add a color when it's expecting a position is most likely a logic error, so it throws in this
 * case.
 * </p><p>
 * The internal buffer is re-used to save on memory allocation. This means at the end of each
 * thread where this class was used (or whenever you want to recreate the buffer), you have to call
 * {@link #freeBuffers()}. Because one buffer is used per thread, this class is therefore
 * thread-safe, as long as you don't share a single instance across multiple threads.
 * </p>
 * // TODO(Joe): indices documentation
 */
public class MeshBuilder {

    // ===== DATA ===== //

    /**
     * The vertex buffer is reused every time a MeshBuilder is created. There is one vertex buffer per thread.
     */
    private static ThreadLocal<ExpandableBuffer> VERTEX_BUFFER = new ThreadLocal<>();
    /**
     * The indices buffer is reused every time a MeshBuilder is created. There is one indices buffer per thread.
     */
    private static ThreadLocal<ExpandableBuffer> INDICES_BUFFER = new ThreadLocal<>();

    private VertexFormat vertexFormat;
    private ExpandableBuffer vertexBuf;
    private ExpandableBuffer indicesBuf;

    private int primitiveDrawType = GL_TRIANGLES;

    /**
     * Size of vertex memory buffer for the BakedMesh. Change at the <b>Start</b> of the program if change is needed
     */
    public static int INITIAL_VERTEX_BUFFER_SIZE = 1048576;
    /**
     * Size of indices memory buffer for the BakedMesh. Change at the <b>Start</b> of the program if change is needed
     */
    public static int INITIAL_INDICES_BUFFER_SIZE = 65536;


    /**
     * The number of bytes written to the vertex buffer so far
     */
    private int verticesSize = 0;
    /**
     * The number of indices written to the indices buffer so far
     */
    private int indicesCount = 0;
    /**
     * The attribute number expected to be written next, in the current vertex
     */
    private int attributeIndex = 0;
    /**
     * When this is true, unexpected calls to builder methods are ignored rather than throwing an exception
     */
    private boolean lenient = false;
    /**
     * When this is true, vertices have to be explicitly ended before moving onto the next vertex
     */
    private boolean explicit = false;


    // ===== MISCELLANEOUS PUBLIC METHODS ===== //

    /**
     * Should be called at the end of each thread where this class is used (typically at the end of the program)
     */

    public static void freeBuffers() {
        if (VERTEX_BUFFER.get() != null) {
            VERTEX_BUFFER.get().free();
            VERTEX_BUFFER.set(null);
        }
        if (INDICES_BUFFER.get() != null) {
            INDICES_BUFFER.get().free();
            INDICES_BUFFER.set(null);
        }
    }

    /**
     * Creates a {@link BakedMesh} with the vertex data and potentially index data added via this builder
     */
    public BakedMesh createMesh() {
        if (attributeIndex != 0)
            throw new IllegalStateException("Cannot create mesh when part way through a vertex");

        vertexBuf.rewind();
        indicesBuf.rewind();

        return vertexFormat.createMesh(vertexBuf.asByteBuffer(), verticesSize, indicesBuf.asByteBuffer(), indicesCount, primitiveDrawType);
    }

    /**
     * Adds index data
     */
    public MeshBuilder indices(int... indices) {
        if (attributeIndex != 0) {
            throw new IllegalStateException("Cannot specify index data part way through a vertex");
        }

        int vertexCount = verticesSize / vertexFormat.getVertexSize();
        for (int index : indices) {
            if (index < 0 || index >= vertexCount)
                throw new IndexOutOfBoundsException("index " + index + ", vertex count " + vertexCount);
        }

        addIndexCount(indices.length);
        for (int index : indices)
            indicesBuf.putInt(index);

        return this;
    }

    /**
     * When the mesh builder is in lenient mode, builder calls that are not expected in the given
     * vertex format will be ignored rather than throwing an exception. This allows you to over-
     * specify attributes in the mesh builder in order to support a range of different vertex
     * formats at once. Do keep in mind that although this gives more freedom, it does remove
     * some of the safety provided by using this class. For more complex vertex formats,
     * {@link #setExplicit(boolean)} may be used in combination with this.
     */
    public MeshBuilder setLenient(boolean lenient) {
        this.lenient = lenient;
        return this;
    }

    /**
     * In explicit mode (off by default), vertices must be ended explicitly. The mesh builder class
     * usually provides enough safety without this; it is usually used in combination with
     * {@link #setLenient(boolean)}
     */
    public MeshBuilder setExplicit(boolean explicit) {
        this.explicit = explicit;
        if (!explicit && attributeIndex == vertexFormat.getAttributes().length)
            attributeIndex = 0;
        return this;
    }
    
    /**
     * Changes the internal buffers of the MeshBuilder to the specified buffers, usually in
     * order to have multiple MeshBuilders at once which don't all try to write to the same
     * buffer. Note it's the caller's responsibility to free these buffers afterwards, the
     * MeshBuilder does not do this automatically.
     */
    public MeshBuilder withBuffers(ExpandableBuffer vertexBuffer, ExpandableBuffer indicesBuffer) {
        if (verticesSize != 0 || indicesCount != 0) {
            throw new IllegalStateException("Cannot change buffers of MeshBuilder after it's started building");
        }
        this.vertexBuf = vertexBuffer;
        this.indicesBuf = indicesBuffer;
        return this;
    }
    
    /**
     * Gets the internal vertex buffer. Note that modifying this buffer in any way while the MeshBuilder
     * is building is an unsafe operation.
     */
    public ExpandableBuffer getVertexBuffer() {
        return vertexBuf;
    }
    
    /**
     * Gets the internal indices buffer. Not that modifying this buffer in any way while the MeshBuilder
     * is building is an unsafe operation.
     */
    public ExpandableBuffer getIndicesBuffer() {
        return indicesBuf;
    }


    // ===== VERTEX ATTRIBUTE METHODS ===== //


    // Position

    public MeshBuilder pos(Vector3f positions) {
        return pos(positions.x, positions.y, positions.z);
    }

    /**
     * Adds a position attribute to the current vertex, ignoring the z-value in the case of a 2D shader
     */
    public MeshBuilder pos(double x, double y, double z) {
        if (!checkAttributeType(VertexAttribute.Type.POSITION))
            return this;

        switch (currentAttribute()) {
            case POSITION2_INTEGER:
                putInt2((int) x, (int) y);
                break;
            case POSITION2_FLOAT:
                putFloat2((float) x, (float) y);
                break;
            case POSITION2_DOUBLE:
                putDouble2(x, y);
                break;
            case POSITION_INTEGER:
                putInt3((int) x, (int) y, (int) z);
                break;
            case POSITION_FLOAT:
                putFloat3((float) x, (float) y, (float) z);
                break;
            case POSITION_DOUBLE:
                putDouble3(x, y, z);
                break;
            default:
                throw new AssertionError("Unknown position attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }

    /**
     * Adds a position attribute to the current vertex, setting the z-value to 0 in the case of a 3D shader
     */
    public MeshBuilder pos(double x, double y) {
        return pos(x, y, 0);
    }


    // Color

    /**
     * Adds a color attribute to the current vertex, setting the alpha value to 255 in the case of
     * the alpha channel being enabled. 0 <= r, g, b <= 255
     */
    public MeshBuilder col(int r, int g, int b) {
        return col(r / 255f, g / 255f, b / 255f);
    }

    /**
     * Adds a color attribute to the current vertex, setting the alpha value to 1.0 in the case of
     * the alpha channel being enabled. 0.0 <= r, g, b <= 1.0
     */
    public MeshBuilder col(float r, float g, float b) {
        return col(r, g, b, 1f);
    }

    /**
     * Adds a color attribute to the current vertex, ignoring the alpha value in the case of the alpha
     * channel being disabled. 0 <= r, g, b, a <= 255
     */
    public MeshBuilder col(int r, int g, int b, int a) {
        return col(r / 255f, g / 255f, b / 255f, a / 255f);
    }

    /**
     * Adds a color attribute to the current vertex, ignoring the alpha value in the case of the alpha
     * channel being disabled. 0.0 <= r, g, b, a <= 1.0
     */
    public MeshBuilder col(float r, float g, float b, float a) {
        if (!checkAttributeType(VertexAttribute.Type.COLOR))
            return this;
        switch (currentAttribute()) {
            case COLOR_FLOAT:
                putFloat3(r, g, b);
                break;
            case COLOR4_FLOAT:
                putFloat4(r, g, b, a);
                break;
            default:
                throw new AssertionError("Unknown color attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }

    /**
     * Adds a color attribute to the current vertex, ignoring the alpha value in the case of the alpha
     * channel being disabled. Input is in the format 0xAARRGGBB. Alpha can be left out if the alpha
     * channel is disabled.
     */
    public MeshBuilder col(int argb) {
        return col((argb >>> 16) & 0xff, (argb >>> 8) & 0xff, argb & 0xff, (argb >>> 24) & 0xff);
    }


    // Texture coordinates

    /**
     * Adds a texture coordinate attribute to the current vertex
     */
    public MeshBuilder tex(double u, double v) {
        if (!checkAttributeType(VertexAttribute.Type.TEXTURE))
            return this;
        switch (currentAttribute()) {
            case TEXTURE_INTEGER:
                putInt2((int) u, (int) v);
                break;
            case TEXTURE_FLOAT:
                putFloat2((float) u, (float) v);
                break;
            case TEXTURE_DOUBLE:
                putDouble2(u, v);
                break;
            default:
                throw new AssertionError("Unknown texture attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }


    // Normal vector

    public MeshBuilder norm(Vector3f norms) {
        return norm(norms.x, norms.y, norms.z);
    }


    /**
     * Adds a normal vector attribute to the current vertex
     */
    public MeshBuilder norm(double x, double y, double z) {
        if (!checkAttributeType(VertexAttribute.Type.NORMAL))
            return this;
        switch (currentAttribute()) {
            case NORMAL_FLOAT:
                putFloat3((float) x, (float) y, (float) z);
                break;
            case NORMAL_DOUBLE:
                putDouble3(x, y, z);
                break;
            default:
                throw new AssertionError("Unknown normal attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }


    /**
     * Adds a tangent vector attribute to the current vertex
     */
    public MeshBuilder tangent(float x, float y, float z) {
        if (!checkAttributeType(VertexAttribute.Type.TANGENT))
            return this;
        switch (currentAttribute()) {
            case TANGENT_FLOAT:
                putFloat3(x, y, z);
                break;
            default:
                throw new AssertionError("Unknown tangent attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }


    /**
     * Adds a bitangent vector attribute to the current vertex
     */
    public MeshBuilder bitangent(float x, float y, float z) {
        if (!checkAttributeType(VertexAttribute.Type.BITANGENT))
            return this;
        switch (currentAttribute()) {
            case BITANGENT_FLOAT:
                putFloat3(x, y, z);
                break;
            default:
                throw new AssertionError("Unknown bitangent attribute: " + currentAttribute().name());
        }
        incAttributeIndex();
        return this;
    }


    // Explicit end vertex for explicit mode

    /**
     * Explicitly ends a vertex in explicit mode
     *
     * @see #setExplicit(boolean)
     */
    public MeshBuilder endVertex() {
        if (explicit) {
            if (attributeIndex != vertexFormat.getAttributes().length)
                throw new IllegalStateException("Cannot end vertex before all attributes have been set");
            attributeIndex = 0;
        }
        return this;
    }


    // ===== INTERNAL IMPLEMENTATION METHODS ===== //


    // Constructor
    MeshBuilder(VertexFormat vertexFormat) {
        this.vertexFormat = vertexFormat;

        vertexBuf = VERTEX_BUFFER.get();
        if (vertexBuf == null) {
            vertexBuf = new ExpandableBuffer(INITIAL_VERTEX_BUFFER_SIZE);
            VERTEX_BUFFER.set(vertexBuf);
        } else {
            vertexBuf.rewind();
        }
        indicesBuf = INDICES_BUFFER.get();
        if (indicesBuf == null) {
            indicesBuf = new ExpandableBuffer(INITIAL_INDICES_BUFFER_SIZE);
            INDICES_BUFFER.set(indicesBuf);
        } else {
            indicesBuf.rewind();
        }
    }

    MeshBuilder(VertexFormat vertexFormat, int primitiveDrawType) {
        this(vertexFormat);
        this.primitiveDrawType = primitiveDrawType;
    }


    // Helper methods to safely add to the vertex buffer
    
    private void putByte(byte a) {
        addSizeToVertexBuffer(1);
        vertexBuf.put(a);
    }

    private void putBytes(byte[] arr) {
        addSizeToVertexBuffer(arr.length);
        vertexBuf.put(arr);
    }

    private void putInt(int a) {
        addSizeToVertexBuffer(4);
        vertexBuf.putInt(a);
    }

    private void putInt2(int a, int b) {
        addSizeToVertexBuffer(8);
        vertexBuf.putInt(a);
        vertexBuf.putInt(b);
    }

    private void putInt3(int a, int b, int c) {
        addSizeToVertexBuffer(12);
        vertexBuf.putInt(a);
        vertexBuf.putInt(b);
        vertexBuf.putInt(c);
    }

    private void putInt4(int a, int b, int c, int d) {
        addSizeToVertexBuffer(16);
        vertexBuf.putInt(a);
        vertexBuf.putInt(b);
        vertexBuf.putInt(c);
        vertexBuf.putInt(d);
    }

    private void putFloat(float a) {
        addSizeToVertexBuffer(4);
        vertexBuf.putFloat(a);
    }

    private void putFloat2(float a, float b) {
        addSizeToVertexBuffer(8);
        vertexBuf.putFloat(a);
        vertexBuf.putFloat(b);
    }

    private void putFloat3(float a, float b, float c) {
        addSizeToVertexBuffer(12);
        vertexBuf.putFloat(a);
        vertexBuf.putFloat(b);
        vertexBuf.putFloat(c);
    }

    private void putFloat4(float a, float b, float c, float d) {
        addSizeToVertexBuffer(16);
        vertexBuf.putFloat(a);
        vertexBuf.putFloat(b);
        vertexBuf.putFloat(c);
        vertexBuf.putFloat(d);
    }

    private void putDouble(double a) {
        addSizeToVertexBuffer(8);
        vertexBuf.putDouble(a);
    }

    private void putDouble2(double a, double b) {
        addSizeToVertexBuffer(16);
        vertexBuf.putDouble(a);
        vertexBuf.putDouble(b);
    }

    private void putDouble3(double a, double b, double c) {
        addSizeToVertexBuffer(24);
        vertexBuf.putDouble(a);
        vertexBuf.putDouble(b);
        vertexBuf.putDouble(c);
    }

    private void putDouble4(double a, double b, double c, double d) {
        addSizeToVertexBuffer(32);
        vertexBuf.putDouble(a);
        vertexBuf.putDouble(b);
        vertexBuf.putDouble(c);
        vertexBuf.putDouble(d);
    }


    // Other utility methods

    private void incAttributeIndex() {
        attributeIndex++;
        if (attributeIndex == vertexFormat.getAttributes().length && !explicit)
            attributeIndex = 0;
    }

    private VertexAttribute currentAttribute() {
        if (attributeIndex == vertexFormat.getAttributes().length) {
            if (explicit) {
                if (lenient)
                    return VertexAttribute.NULL;
                else
                    throw new IllegalStateException("Tried to specify too many vertex attributes in one vertex");
            } else {
                throw new AssertionError();
            }
        }
        return vertexFormat.getAttributes()[attributeIndex];
    }

    private boolean checkAttributeType(VertexAttribute.Type attempted) {
        if (currentAttribute().getType() != attempted) {
            if (lenient)
                return false;
            else
                throw new IllegalStateException("Tried to add an attribute of type " + attempted.name() + ", expected " + currentAttribute().getType().name());
        }
        return true;
    }

    private void addSizeToVertexBuffer(int bytesToWrite) {
        verticesSize += bytesToWrite;
    }

    private void addIndexCount(int extraIndices) {
        indicesCount += extraIndices;
    }
}
