package render.shader;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import render.util.VertexFormat;

public class Shaders {

    public static SceneShader sceneShader;

    public static void loadShaders(){
        sceneShader = new SceneShader();
    }


    public static class SceneShader extends ShaderProgram {

        public final Uniform<Matrix4f> modelMat;
        public final Uniform<Matrix4f> projViewMat;
        public final Uniform<Vector3f> lightPos;

        public SceneShader() {


            super("scene.vert", "scene.frag",
                    VertexFormat.POS_NORM,
                    "pos", "norm");
            bind();
            modelMat = createUniform("modelMat", UniformType.MATRIX4F);
            projViewMat = createUniform("projViewMat", UniformType.MATRIX4F);
            lightPos = createUniform("lightPos", UniformType.VECTOR3F);
            unbind();
        }

    }

}
