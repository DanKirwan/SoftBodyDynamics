package render.objLoader;

//import entity.phys.CollisionMesh;
import render.util.BakedMesh;
import render.util.MeshBuilder;
import render.util.VertexAttribute;
import render.util.VertexFormat;
import sun.security.provider.certpath.Vertex;
import util.VectorUtil;
import org.joml.*;


import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

/**
 * This class will read and create {@link BakedMesh} from an OBJ file.
 *
 * <p>
 * Example usage:
 * <blockquote><pre>
 *     InputStream is = new FileInputStream(new File("my_obj.ob"));
 *     ObjLoader loader = new ObjLoader(is);
 *     BakedMesh mesh = loader.load();
 * </pre></blockquote>
 * </p>
 */
public class ObjLoader {

    /*
     * https://imgflip.com/i/2ox6xf
     *
     *    ob ob ob      ob ob ob ob
     * ob ob ob ob ob   ob ob ob ob ob
     * ob          ob   ob          ob
     * ob          ob   ob          ob
     * ob          ob   ob          ob
     * ob          ob   ob ob ob ob
     * ob          ob   ob ob ob ob ob
     * ob          ob   ob          ob
     * ob          ob   ob          ob
     * ob          ob   ob          ob
     * ob ob ob ob ob   ob ob ob ob ob
     *    ob ob ob      ob ob ob ob
     *
     */


    /*
     * Aside from doing the obvious, this class has one extra hurdle it deals with internally.
     * In the OBJ file, each vertex on each triangle (aka tri aka face) has 3 indices per vertex:
     * one for each of pos, texture coordinates and normal vector. However, OpenGL only
     * allows for one index per vertex, pointing to a unique triple of pos, colourTex and norm.
     * To solve this, when the loader loads the vertices of the tris, it puts them in a lookup
     * map called indices, then ensures each triple of pos, colourTex and norm are unique in the final
     * mesh.
     */

    private Scanner scan;

    private List<Vector3f> positions = new ArrayList<>();
    private List<Vector4f> colors = new ArrayList<>();
    private List<Vector2f> textures = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private Vector3f[] tangents = null;
    private Vector3f[] bitangents = null;
    private HashMap<Vertex, Integer> indices = new HashMap<>();
    private List<Vertex> vertexList =  new ArrayList<>();
    private List<Vector3i> tris = new ArrayList<>();
    private List<Vector2i> lines = new ArrayList<>();

    /**
     * Will read the OBJ file from the given {@link Scanner}
     */
    public ObjLoader(Scanner scan) {
        this.scan = scan;
    }

    /**
     * Will read the OBJ file from the given {@link InputStream}
     */
    public ObjLoader(InputStream in) {
        this(new Scanner(in));
    }

    /**
     * Will read the OBJ file from the given {@link Reader}
     */
    public ObjLoader(Reader reader) {
        this(new Scanner(reader));
    }

    /**
     * Loads the OBJ file the loader was constructed with, and constructs and returns a {@link BakedMesh}
     *
     * @throws ObjFormatException If the OBJ format error was detected
     */
    public BakedMesh load(VertexFormat vertexFormat) throws ObjFormatException {
        return load(vertexFormat, GL_TRIANGLES);
    }

    /**
     * Loads an OBJ file the loader was constructed with with a specified primitive Type in the obj file, eg GL_TRIANGLES or GL_LINES
     *
     * @throws ObjFormatException If the OBJ format error was detected
     */
    public BakedMesh load(VertexFormat vertexFormat, int primitiveType) throws ObjFormatException {
        if (!VertexFormat.POS_TEX_COL4_NORM_TANG_BITANG.isLenientlyCompatible(vertexFormat))
            throw new IllegalStateException("Vertex format incompatible with OBJ models");

        // Read from the file into the fields
        int lineNo = 1;
        while (scan.hasNextLine()) {
            parseLine(lineNo++, scan.nextLine(),primitiveType);
        }

        // pre processing for tangent and bitangent
        if(vertexFormat.hasAttributeType(VertexAttribute.Type.TANGENT) && vertexFormat.hasAttributeType(VertexAttribute.Type.BITANGENT)) {
            tangents = new Vector3f[vertexList.size()];
            bitangents = new Vector3f[vertexList.size()];

            for(Vector3i face : tris) {
                if(textures.size() == 0) {
                    Vector3f v = new Vector3f(0, 0, 1);
                    tangents[face.x] = v;
                    tangents[face.y] = v;
                    tangents[face.z] = v;

                    bitangents[face.x] = v;
                    bitangents[face.y] = v;
                    bitangents[face.z] = v;
                } else {
                    //genTangAndBitang(face);
                }
            }
        }

        // Start building the mesh
        MeshBuilder meshBuilder = BakedMesh.builder(vertexFormat, primitiveType);
        meshBuilder.setLenient(true).setExplicit(true);

        // Create our own vertex list, which may be longer than theirs, since GL only allows us 1 index list
        for (int i = 0; i < vertexList.size(); i++) {

            Vertex vertex = vertexList.get(i);

            if (vertex.pos != -1 && vertex.pos <= 0 || vertex.pos > positions.size())
                throw new ObjFormatException(vertex.lineNo, "out of bounds vertex pos index: " + vertex.pos);
            if (vertex.tex != -1 && vertex.tex <= 0 || vertex.tex > textures.size())
                throw new ObjFormatException(vertex.lineNo, "out of bounds texture index: " + vertex.tex);
            if (vertex.norm != -1 && vertex.norm <= 0 || vertex.norm > normals.size())
                throw new ObjFormatException(vertex.lineNo, "out of bounds normal index: " + vertex.norm);

            Vector3f pos = vertex.pos != -1 ? positions.get(vertex.pos - 1) : new Vector3f(0, 0, 0);
            Vector4f col = vertex.pos != -1 ? colors.get(vertex.pos - 1) : new Vector4f(1, 1, 1, 1);
            Vector2f tex = vertex.tex != -1 ? textures.get(vertex.tex - 1) : new Vector2f(0, 0);
            Vector3f norm = vertex.norm != -1 ? normals.get(vertex.norm - 1) : new Vector3f(1, 0, 0);

            Vector3f tang = new Vector3f();
            Vector3f bitang = new Vector3f();
            if(tangents != null && bitangents != null) {
                tang = tangents[i];
                bitang = bitangents[i];

            }

            //System.err.println("Vertex: Pos: " + pos + " Normal: " + norm + " Tangent: " + tang + " Bitangent: " + bitang + " Face: " + i);

            meshBuilder.pos(pos.x, pos.y, pos.z).tex(tex.x, tex.y).col(col.x, col.y, col.z, col.w)
                    .norm(norm.x, norm.y, norm.z).tangent(tang.x, tang.y, tang.z)
                    .bitangent(bitang.x, bitang.y, bitang.z).endVertex();
        }

        // Add our indices list
        if(primitiveType == GL_TRIANGLES) {
            for (Vector3i tri : tris) {
                meshBuilder.indices(tri.x, tri.y, tri.z);
            }
        } else if (primitiveType == GL_LINES) {
            for(Vector2i line : lines) {
                meshBuilder.indices(line.x, line.y);
            }
        }

        return meshBuilder.createMesh();
    }
    /*
    public CollisionMesh loadCollisionMesh() throws ObjFormatException {
        // Read from the file into the fields
        int lineNo = 1;
        while (scan.hasNextLine()) {
            parseLine(lineNo++, scan.nextLine(), GL_TRIANGLES);
        }

        List<Vector3f> positions = new ArrayList<>();
        Vector3f bbMin = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f bbMax = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        List<Vector3f> normals = new ArrayList<>();

        for (Vertex vertex : vertexList) {
            if (vertex.pos != -1 && vertex.pos <= 0 || vertex.pos > this.positions.size())
                throw new ObjFormatException(vertex.lineNo, "out of bounds vertex pos index: " + vertex.pos);
            if (vertex.norm != -1 && vertex.norm <= 0 || vertex.norm > this.normals.size())
                throw new ObjFormatException(vertex.lineNo, "out of bounds normal index: " + vertex.norm);

            Vector3f pos = vertex.pos != -1 ? this.positions.get(vertex.pos - 1) : new Vector3f(0, 0, 0);
            positions.add(pos);
            VectorUtil.componentWiseMin(bbMin, pos);
            VectorUtil.componentWiseMax(bbMax, pos);
            normals.add(vertex.norm != -1 ? this.normals.get(vertex.norm - 1) : new Vector3f(1, 0, 0));
        }

        CollisionMesh mesh = new CollisionMesh(positions, normals, tris, bbMin, bbMax);
        mesh.removeRedundancies();
        return mesh;
    }
 */

    private void genTangAndBitang(Vector3i face) {

        Vertex vert1 = vertexList.get(face.x);
        Vertex vert2 = vertexList.get(face.y);
        Vertex vert3 = vertexList.get(face.z);


        Vector3f p1 = positions.get(vert1.pos - 1);
        Vector3f p2 = positions.get(vert2.pos - 1);
        Vector3f p3 = positions.get(vert3.pos - 1);

        Vector2f t1 = vert1.tex != -1 ? textures.get(vert1.tex - 1) : new Vector2f(0, 0);
        Vector2f t2 = vert2.tex != -1 ? textures.get(vert2.tex - 1) : new Vector2f(1, 0);
        Vector2f t3 = vert3.tex != -1 ? textures.get(vert3.tex - 1) : new Vector2f(0, 1);

        Vector3f n1 = normals.get(vert1.norm - 1);
        Vector3f n2 = normals.get(vert2.norm - 1);
        Vector3f n3 = normals.get(vert3.norm - 1);

        //Defining the difference vectors
        Vector3f e1 = p2.sub(p1, new Vector3f());
        Vector3f e2 = p3.sub(p1, new Vector3f());

        Vector2f uv1 = t2.sub(t1, new Vector2f());
        float u1 = uv1.x;
        float v1 = uv1.y;


        Vector2f uv2 = t3.sub(t1, new Vector2f());
        float u2 = uv2.x;
        float v2 = uv2.y;

        //calculating final result
        float invDet = 1/(u1*v2 - u2*v1);

        Vector3f tang = new Vector3f();

        tang.x = (v2*e1.x - v1*e2.x) * invDet;
        tang.y = (v2*e1.y - v1*e2.y) * invDet;
        tang.z = (v2*e1.z - v1*e2.z) * invDet;


        //calculating for each

        Vector3f tang1 = new Vector3f(tang);
        tang1 = tang1.sub(n1.mul(tang1.dot(n1), new Vector3f()));

        Vector3f tang2 = new Vector3f(tang);
        tang2 = tang2.sub(n2.mul(tang2.dot(n2), new Vector3f()));

        Vector3f tang3 = new Vector3f(tang);
        tang3 = tang3.sub(n3.mul(tang3.dot(n3), new Vector3f()));

        tangents[face.x] = tang1.normalize();
        tangents[face.y] = tang2.normalize();
        tangents[face.z] = tang3.normalize();

        bitangents[face.x] = n1.cross(tang1, new Vector3f()).normalize();
        bitangents[face.y] = n2.cross(tang2, new Vector3f()).normalize();
        bitangents[face.z] = n3.cross(tang3, new Vector3f()).normalize();

        //System.err.println("Pos: " + p1 + " Normal: " + n1 + " Tangent: " + tang1 + " Face: " + face.x);

        //:)
    }



    private void parseLine(int lineNo, String line, int primitiveType) throws ObjFormatException {
        // ignore comments
        if (line.startsWith("#"))
            return;

        String[] parts = line.split(" ");
        switch (parts[0]) {
            case "v": {
                if (parts.length != 4 && parts.length != 5 && parts.length != 7 && parts.length != 8)
                    throw new ObjFormatException(lineNo, "vertex contains wrong number of parameters");
                positions.add(new Vector3f(parseFloat(lineNo, parts[1]), parseFloat(lineNo, parts[2]), parseFloat(lineNo, parts[3])));
                if (parts.length >= 7) {
                    colors.add(new Vector4f(parseFloat(lineNo, parts[4]), parseFloat(lineNo, parts[5]), parseFloat(lineNo, parts[6]), parts.length > 7 ? parseFloat(lineNo, parts[7]) : 1));
                } else {
                    colors.add(new Vector4f(1, 1, 1, 1));
                }
                break;
            }
            case "vt": {
                if (parts.length != 3)
                    throw new ObjFormatException(lineNo, "texture coordinate doesn't contain 2 coordinates");
                textures.add(new Vector2f(parseFloat(lineNo, parts[1]), parseFloat(lineNo, parts[2])));
                break;
            }
            case "vn": {
                if (parts.length != 4)
                    throw new ObjFormatException(lineNo, "normal vector doesn't contain 3 coordinates");
                normals.add(new Vector3f(parseFloat(lineNo, parts[1]), parseFloat(lineNo, parts[2]), parseFloat(lineNo, parts[3])));
                break;
            }
            case "f": {
                if(primitiveType != GL_TRIANGLES)
                    throw new ObjFormatException(lineNo, "expected lines mesh, recieved face definitions");
                if (parts.length != 4)
                    throw new ObjFormatException(lineNo, "face doesn't contain 3 vertices");

                tris.add(new Vector3i(parseVertexToIndex(lineNo, parts[1]), parseVertexToIndex(lineNo, parts[2]), parseVertexToIndex(lineNo, parts[3])));
                break;
            }
            case "l": {
                if (primitiveType != GL_LINES)
                    throw new ObjFormatException(lineNo, "expected triangle mesh, recieved line definitions");
                if (parts.length != 3)
                    throw new ObjFormatException(lineNo, "line doesn't contain 2 vertices");

                lines.add(new Vector2i(parseVertexToIndex(lineNo, parts[1]),parseVertexToIndex(lineNo, parts[2])));
            }
            default: {
                // Tutorial said to allow this ¯\_(ツ)_/¯
                //throw new ObjFormatException(lineNo, "unsupported instruction: " + parts[0]);
            }
        }
    }

    private float parseFloat(int lineNo, String str) throws ObjFormatException {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException e) {
            throw new ObjFormatException(lineNo, "invalid float: " + str);
        }
    }

    private int parseInt(int lineNo, String str) throws ObjFormatException {
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ObjFormatException(lineNo, "invalid integer: " + str);
        }
    }

    private Vertex parseVertex(int lineNo, String str) throws ObjFormatException {
        String[] parts = str.split("/");
        if (parts.length > 3)
            throw new ObjFormatException(lineNo, "too many parts to the vertex: " + str);

        int pos = parseInt(lineNo, parts[0]);
        int tex = parts.length > 1 && !parts[1].isEmpty() ? parseInt(lineNo, parts[1]) : -1;
        int norm = parts.length > 2 ? parseInt(lineNo, parts[2]) : -1;

        return new Vertex(lineNo, pos, tex, norm);
    }

    private int parseVertexToIndex(int lineNo, String str) throws ObjFormatException {
        Vertex vert = parseVertex(lineNo, str);

        // This is where the indices reconstruction magic happens
        Integer index = indices.get(vert);
        if (index != null)
            return index;
        int newIndex = indices.size();
        indices.put(vert, newIndex);
        vertexList.add(vert);
        return newIndex;
    }

    private static class Vertex {
        private int lineNo;
        private int pos;
        private int tex;
        private int norm;

        public Vertex(int lineNo, int pos, int tex, int norm) {
            this.lineNo = lineNo;
            this.pos = pos;
            this.tex = tex;
            this.norm = norm;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Vertex vertex = (Vertex) o;

            if (pos != vertex.pos) return false;
            if (tex != vertex.tex) return false;
            return norm == vertex.norm;
        }

        @Override
        public int hashCode() {
            int result = pos;
            result = 31 * result + tex;
            result = 31 * result + norm;
            return result;
        }
    }

}
