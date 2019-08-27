import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.Version;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.CallbackI;
import physics.Mesh;
import physics.Particle;
import physics.ParticleSimulator;
import render.Window;
import render.objLoader.ObjLoader;
import render.render3D.Camera;
import render.render3D.Transformation;
import render.shader.Shaders;
import render.util.BakedMesh;
import util.Mathf;
import util.MeshHandler;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;
import static render.shader.Shaders.sceneShader;

public class TestSetup {

    private static Window window;
    private static Matrix4f projMat;
    private static Matrix4f viewMat;
    private static Matrix4f projViewMat;
    private static Callback errorCallback;

    private static Mesh sphereMesh;
    private static Camera cam;
    private static ParticleSimulator particleSim;

    public static void main(String[] args) {
        System.out.println("LWJGL Version " + Version.getVersion() + " is working.");



        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        createWindow();
        GL.createCapabilities();
        errorCallback = GLUtil.setupDebugMessageCallback();



        //Load object
        sphereMesh = new Mesh("bullet.ob");
        cam = new Camera(window);
        particleSim = new ParticleSimulator();
        particleSim.addParticle(new Particle(new Vector3f(0,0,0), new Vector3f(0,20f, 0),0.5f, sphereMesh));





        //Render Object
        projMat = Transformation.getProjectionMatrix( 1f, window.getAspectRatio());
        viewMat = new Matrix4f();
        projViewMat = new Matrix4f().mul(projMat).mul(viewMat);

        Shaders.loadShaders();

        glViewport(0, 0, window.getWidth(), window.getHeight());
        glEnable(GL_DEPTH_TEST);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);


        sceneShader.bind();
        sceneShader.projViewMat.set(projViewMat);
        sceneShader.lightPos.set(new Vector3f(0,2,0));


        glEnable(GL_DEPTH_TEST);



        float timeStep = 0;
        boolean wasPDown = false;
        boolean paused = false;
        //TODO remove this and set up a sim loop
        while (!window.shouldClose()) {
            glfwPollEvents();
            cam.doCameraMovement();

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);


            sceneShader.bind();
            projViewMat = new Matrix4f().mul(projMat).mul(cam.getViewMat());
            sceneShader.projViewMat.set(projViewMat);


            //sceneShader.lightPos.set(new Vector3f(Mathf.cos(-timeStep*0.01f)*10, 5, Mathf.sin(-timeStep*0.01f)*10 - 5));
            glClearColor(0.4f,0.4f,0.4f,1);




            if(window.isKeyDown(GLFW_KEY_P)) {
                if(!wasPDown) paused = !paused;
                wasPDown = true;
            } else {
                wasPDown = false;
            }

            if(!paused) {

                if (timeStep % 2 == 0) {
                    particleSim.addParticle(new Particle(sphereMesh).randMass(0.5f).randVelocity());
                }

                particleSim.tick();
            }

            particleSim.drawWorld();

            sceneShader.unbind();



            window.endFrame();


            timeStep++;


            try {
                Thread.sleep(Constants.TIME_STEP);
            } catch (InterruptedException ignore) {
            }
        }



    }


    private static void createWindow() {
        window = new Window(Constants.NAME + " " + Constants.VERSION);
        window.makeContextCurrent();


        window.show();
        window.setResizeCallback(() -> {
            projMat = Transformation.getProjectionMatrix( 1f, window.getAspectRatio());
            //currentScreen.onResize(window.getWidth(), window.getHeight());
        });


    }







}

