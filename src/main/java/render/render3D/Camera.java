package render.render3D;

import org.joml.*;
import render.Window;
import util.MathUtil;
import util.Mathf;
import util.VectorUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;

public class Camera {

    private static final Quaternionf IDENTITY_CAMERA_ROTATION = VectorUtil.eulerToQuaternion(Mathf.PI, 0, 0);


    private final Vector3f position;
    private final Quaternionf rotation;
    private final Matrix4f viewMatrix;
    private final Window window;


    private double prevMouseX;
    private double prevMouseY;
    private boolean dragging = true;
    private boolean wasTDown = false;
    private float yaw = 0;
    private float pitch = 0;


    private float SPIN_SENSITIVITY = 0.003f;
    private float MOVEMENT_SENSITIVITY = 0.05f;

    public Camera(Window win) {
        position = new Vector3f(0, 0, 0);
        rotation = new Quaternionf();
        viewMatrix = new Matrix4f();
        window = win;
    }

    public Camera(Vector3f position, Quaternionf rotation, Window win) {
        this.position = position;
        this.rotation = rotation;
        viewMatrix = new Matrix4f();
        window = win;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
    }

    public Quaternionfc getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
    }

    public Matrix4f getViewMat() {
        viewMatrix.identity();
        viewMatrix.rotate(rotation);
        viewMatrix.translate(-position.x(), -position.y(), -position.z());
        return viewMatrix;
    }

    public void doCameraMovement() {


        double mouseX = window.getMouseX();
        double mouseY = window.getMouseY();


        //T to start moving, ESC to stop
        if(window.isKeyDown(GLFW_KEY_T)) {
            dragging = true;
            window.disableCursor();
        }

        if(window.isKeyDown(GLFW_KEY_ESCAPE)) {
            dragging = false;
            window.showCursor();
        }

        if (dragging) {
            window.disableCursor();
            double dx = mouseX - prevMouseX;
            double dy = mouseY - prevMouseY;
            prevMouseX = mouseX;
            prevMouseY = mouseY;
            if (dx != 0 || dy != 0) {
                yaw = MathUtil.wrapAngle(yaw + (float) dx * SPIN_SENSITIVITY);
                pitch = MathUtil.clamp(pitch + (float) dy * SPIN_SENSITIVITY, -Mathf.PI / 2 + 0.001f, Mathf.PI / 2 - 0.001f);

                rotation.identity().rotateAxis(pitch, 1,0,0).rotateAxis(yaw, 0,1,0);
            }
        }



        float forwards = 0;
        float sideways = 0;
        float vertical = 0;
        if (window.isKeyDown(GLFW_KEY_W))
           forwards += MOVEMENT_SENSITIVITY;
        if (window.isKeyDown(GLFW_KEY_S))
           forwards -= MOVEMENT_SENSITIVITY;
        if (window.isKeyDown(GLFW_KEY_D))
            sideways += MOVEMENT_SENSITIVITY;
        if (window.isKeyDown(GLFW_KEY_A))
            sideways -= MOVEMENT_SENSITIVITY;
        if(window.isKeyDown(GLFW_KEY_SPACE))
            vertical += MOVEMENT_SENSITIVITY;
        if(window.isShiftKeyDown())
            vertical -= MOVEMENT_SENSITIVITY;


        position.add(rotation.positiveZ(new Vector3f()).mul(1,0,1).normalize().mul(forwards).negate());
        position.add(rotation.positiveX(new Vector3f()).mul(1,0,1).normalize().mul(sideways));
        position.add(0,vertical, 0);

        if(window.isKeyDown(GLFW_KEY_O)) {
            position.set(0,0,0);
            rotation.identity();
            yaw = 0;
            pitch = 0;
        }

    }



}