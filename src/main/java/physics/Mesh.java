package physics;

import org.joml.Vector3f;
import render.util.BakedMesh;
import render.util.MeshBuilder;
import render.util.VertexFormat;
import sun.security.provider.certpath.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class Mesh {



    private ArrayList<Vector3f> positions = new ArrayList<>();
    private ArrayList<Vector3f> normals = new ArrayList<>();

    private ArrayList<Triangle> primitives = new ArrayList<>();


    public final String location;

    public Mesh(String location) {
        this.location = location;

        try {
            loadMesh(location);
        } catch(IOException e) {

        }

    }


    private void loadMesh(String location) throws IOException {
        InputStream is = Mesh.class.getResourceAsStream("/models/" + location);

        if (is == null) {
            throw new IllegalStateException("Model not found: " + location);
        }

        Scanner scan = new Scanner(is);
        int lineNo = 1;

        while(scan.hasNextLine()) {
            parseLine(lineNo, scan.nextLine());
        }

        lineNo++;

    }


    private void parseLine(int lineNo, String line) throws IOException {

        if(line.startsWith("#"))
            return;

        String[] parts = line.split(" ");

        switch(parts[0]) {
            case "v" : {
                if(parts.length != 4) {
                    throw new IOException("Invalid position data at line " + lineNo);
                } else {
                    positions.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                }
                break;
            }
            case "vn" : {
                if(parts.length != 4) {
                    throw new IOException("Invalid normal data at line " + lineNo);
                } else {
                    normals.add(new Vector3f(Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])));
                }
                break;
            }
            case "f" : {
                if (parts.length != 4) {
                    throw new IOException("Invalid face data at line " + lineNo);
                } else {
                    String[] vert1 = parts[1].split("/");
                    String[] vert2 = parts[2].split("/");
                    String[] vert3 = parts[3].split("/");

                    primitives.add(new Triangle(Integer.parseInt(vert1[0]), Integer.parseInt(vert2[0]), Integer.parseInt(vert3[0]),
                            Integer.parseInt(vert1[2]), Integer.parseInt(vert2[2]), Integer.parseInt(vert3[2])));
                break;
                }
            }

            default: {

            }

        }
    }


    public BakedMesh bakeMesh() {
        MeshBuilder builder = BakedMesh.builder(VertexFormat.POS_NORM);

        for(Triangle tri : primitives) {
            builder.pos(positions.get(tri.posInds[0] - 1))
                    .norm(normals.get(tri.normInds[0] - 1))
                    .pos(positions.get(tri.posInds[1] - 1))
                    .norm(normals.get(tri.normInds[1] - 1))
                    .pos(positions.get(tri.posInds[2] - 1))
                    .norm(normals.get(tri.normInds[2] - 1));
        }

        return builder.createMesh();
    }






    private class Triangle {

        private int[] posInds = new int[3];
        private int[] normInds = new int[3];

        Triangle(int pos1, int pos2, int pos3, int norm1, int norm2, int norm3) {
            posInds[0] = pos1;
            posInds[1] = pos2;
            posInds[2] = pos3;


            normInds[0] = norm1;
            normInds[1] = norm2;
            normInds[2] = norm3;
        }
    }
}
