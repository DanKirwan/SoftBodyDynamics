package render.render3D;


import render.RenderEntity;
import util.Mathf;
import util.VectorUtil;
import org.joml.*;

public class Transformation {

    private static final Quaternionf IDENTITY_CAMERA_ROTATION = VectorUtil.eulerToQuaternion(Mathf.PI/2, 0, 0);

    private static final Matrix4f projectionMatrix = new Matrix4f();

    private static final Matrix4f worldMatrix = new Matrix4f();

    private static final Matrix4f viewMatrix = new Matrix4f();

    private static final Matrix4f modelViewMatrix = new Matrix4f();

    private static final Matrix4f orthoMatrix = new Matrix4f();

    private static final Matrix3f normalMatrix = new Matrix3f();

    public Transformation() {
    }

    /**
     * Generates a projection matrix
     *
     * @param fov The field of view in <b>Radians</b>
     * @param aspectRatio The width/height of window
     * @param zNear Closest the frustrum will render
     * @param zFar Farthest render distance
     * @return A projection matrix
     */
    public static final Matrix4f getProjectionMatrix(float fov, float aspectRatio, float zNear, float zFar) {
        projectionMatrix.identity();
        projectionMatrix.perspective(fov, aspectRatio, zNear, zFar);
        return projectionMatrix;
    }

    public static final Matrix4f getProjectionMatrix(float fov, float aspectRatio) {
        //TODO (Dan) Change this to stop clipping when physics is done
        return getProjectionMatrix(fov,aspectRatio,0.1f, 500f);

    }

    public static final Matrix4f getProjectionViewMatrix(Matrix4f projMatrix, render.render3D.Camera camera){
        return projMatrix.mul(getViewMatrix(camera));

    }



    public static Matrix4f getWorldMatrix(Matrix4f mat) {
        worldMatrix.set(mat);
        return worldMatrix;
    }


    public static Matrix4f getModelViewMatrix(RenderEntity renderEntity, Matrix4f viewMatrix) {
        modelViewMatrix.set(renderEntity.getModelMatrix());
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(modelViewMatrix);
    }


    public static Matrix4f getWorldMatrix(Vector3fc pos, Quaternionfc rot, float scale){
        worldMatrix.identity().translate(pos).rotate(rot).scale(scale);
        return worldMatrix;
    }



    public static Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Quaternionfc rotation = camera.getRotation();

        viewMatrix.identity();
        viewMatrix.rotate(IDENTITY_CAMERA_ROTATION);
        viewMatrix.rotate(rotation.invert(new Quaternionf()));
        viewMatrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        return viewMatrix;
    }

    public static Matrix4f getViewMatrix(Vector3fc position, Quaternionfc rotation) {
        viewMatrix.identity();

        viewMatrix.rotate(IDENTITY_CAMERA_ROTATION);
        viewMatrix.rotate(rotation.invert(new Quaternionf()));
        viewMatrix.translate(-position.x(), -position.y(), -position.z());
        return viewMatrix;
    }


    public static Matrix4f getRotationViewMatrix(Camera camera) {
        Quaternionfc rotation = camera.getRotation();
        viewMatrix.identity();
        viewMatrix.rotate(IDENTITY_CAMERA_ROTATION);
        viewMatrix.rotate(rotation.invert(new Quaternionf()));
        return viewMatrix;
    }

    /**
     *
     * @param modelMatrix A model matrix that translate world coordinates to view coordinates
     * @return A normal matrix which is applied to vertexNormals to translate them to world coordinates
     */
    public static Matrix3f getNormalMatrix(Matrix4f modelMatrix) {
        normalMatrix.set(modelMatrix);
        //normalMatrix.normal();
        return normalMatrix;
    }

    public static Matrix4f getOrthoProjectionMatrix2D() {
        return orthoMatrix;
    }

    public static Matrix4f getOrthoProjectionMatrix2D(float left, float right, float bottom, float top) {
        orthoMatrix.identity();
        orthoMatrix.scale(1, -1, 1);
        orthoMatrix.ortho2D(left, right, top, bottom);
        return orthoMatrix;
    }



}