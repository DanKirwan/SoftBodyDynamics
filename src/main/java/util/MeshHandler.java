package util;

import render.objLoader.ObjFormatException;
import render.objLoader.ObjLoader;
import render.util.BakedMesh;
import render.util.VertexFormat;

import java.io.InputStream;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;

public  class MeshHandler {

    public static BakedMesh loadMesh(String location) {

        InputStream is = MeshHandler.class.getResourceAsStream("/models/" + location);

        if (is == null) {
            throw new IllegalStateException("Model not found: " + location);
        }
        BakedMesh mesh;
        try {
            mesh = new ObjLoader(is).load(VertexFormat.POS_NORM, GL_TRIANGLES);
        } catch (ObjFormatException e) {
            throw new IllegalStateException("OBJ format error in " + location, e);
        }
        return mesh;
    }

}
