package render;

import render.util.BakedMesh;
import render.util.Drawable;
import org.joml.*;

import java.io.IOException;

import static org.lwjgl.opengl.GL11.*;

public abstract class RenderEntity implements Drawable {

    protected Drawable drawable;


    protected final Vector3f scale;
    protected final Quaternionf rotation;
    protected Matrix4f modelMatrix;


    protected RenderEntity() {
        scale = new Vector3f();
        rotation = new Quaternionf();
    }

    public RenderEntity(Drawable drawable) {
        this.drawable = drawable;
        this.scale = new Vector3f(1, 1, 1);
        this.rotation = new Quaternionf();
        this.modelMatrix = new Matrix4f();
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public Vector3f getPosition() {
        return new Vector3f(getX(), getY(), getZ());
    }

    public float getX() {
        return modelMatrix.m30();
    }

    public float getY() {
        return modelMatrix.m31();
    }

    public float getZ() {
        return modelMatrix.m32();
    }

    public void setX(float x) {
        modelMatrix.m30(x);
    }

    public void setY(float y) {
        modelMatrix.m31(y);
    }

    public void setZ(float z) {
        modelMatrix.m32(z);
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setPosition(float x, float y, float z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    public void setPosition(Vector3fc pos) {
        setPosition(pos.x(), pos.y(), pos.z());
    }

    public Quaternionfc getRotation() {
        return rotation;
    }

    public void setRotation(Quaternionfc rotation) {
        modelMatrix.rotate(this.rotation.invert(new Quaternionf()));
        this.rotation.set(rotation);
        modelMatrix.rotate(rotation);
    }

    public void scale(float scale) {
        scale(scale, scale, scale);
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        if (scaleX == 0 || scaleY == 0 || scaleZ == 0)
            throw new IllegalArgumentException("Cannot have 0 scale");
        scale.x *= scaleX;
        scale.y *= scaleY;
        scale.z *= scaleZ;
        modelMatrix.scale(scaleX, scaleY, scaleZ);
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        scale(scaleX / getScaleX(), scaleY / getScaleY(), scaleZ / getScaleZ());
    }

    public Vector3f getScale() {
        return scale;
    }

    public float getScaleX() {
        return scale.x;
    }

    public float getScaleY() {
        return scale.y;
    }

    public float getScaleZ() {
        return scale.z;
    }

    public Matrix4f getModelMatrix() {
        return modelMatrix;
    }

    @Override
    public void draw() {
        preDraw();
        drawable.draw();
        postDraw();
    }

    /*public void drawOutline(Vector4f colour, float width) {
        glEnable(GL_STENCIL_TEST);

        glDisable(GL_DEPTH_TEST);


        glStencilOp(GL_KEEP,GL_KEEP,GL_REPLACE);
        glClear(GL_STENCIL_BUFFER_BIT);
        glStencilFunc(GL_ALWAYS, 1, 0xFF);
        glStencilMask(0xFF);

        //Drawing the stencil outline for the shape
        glColorMask(false,false,false,false);
        glDepthMask(false);
        shaders.noLightCol3D.bind();
        shaders.noLightCol3D.modelMatrix.set(modelMatrix);
        drawable.draw();
        glColorMask(true, true, true, true);
        glDepthMask(true);

        glStencilFunc(GL_EQUAL, 0, 0xFF);
        glStencilMask(0x00); // disable writing to the stencil buffer

        preDrawOutline(colour,width);
        drawable.draw();
        postDrawOutline(width);

        glStencilMask(0xFF);
        glDisable(GL_STENCIL_TEST);
        glEnable(GL_DEPTH_TEST);

    }*/



    protected abstract void preDraw();

    protected abstract void postDraw();

    protected void preDrawOutline(Vector4f colour, float width) {

    }

    protected void postDrawOutline(float width){

    }

    public void free() {
        drawable.free();
    }

}
