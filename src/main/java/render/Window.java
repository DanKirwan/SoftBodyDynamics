package render;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import render.util.OS;

import java.awt.*;
import java.nio.IntBuffer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;


public class Window {

    private static final int[][] ACCEPTABLE_RESOLUTIONS = {
            {1024, 576},
            {1152, 648},
            {1280, 720},
            {1366, 768},
            {1600, 900},
            {1920, 1080},
            {2560, 1440},
            {3840, 2160},
            {7680, 4320}
    };

    private int width;
    private int height;
    private String title;
    private long pointer;
    private Runnable resizeCallback = () -> {
    };
    private IntConsumer keyPressCallback = keyCode -> {
    };
    private IntConsumer keyReleaseCallback = keyCode -> {
    };
    private IntConsumer keyRepeatCallback = keyCode -> {
    };
    private IntConsumer charTypedCallback = codepoint -> {
    };
    private IntConsumer mousePressCallback = mouseButton -> {
    };
    private IntConsumer mouseReleaseCallback = mouseButton -> {
    };
    private DoubleConsumer mouseScrollCallback = scrollAmt -> {
    };
    private boolean resized = false;
    private double mouseX;
    private double mouseY;
    private boolean mouseInside;
    private int windowedX, windowedY, windowedWidth, windowedHeight;

    public Window(String title) {

        int availableX, availableY, availableWidth, availableHeight;
        int frameDecorationLeft, frameDecorationTop, frameDecorationRight, frameDecorationBottom;
        {
            // Hide the dirty AWT stuff in a {} block, that makes it better, right?
            GraphicsConfiguration gc = GraphicsEnvironment
                    .getLocalGraphicsEnvironment().getScreenDevices()[0].getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();
            Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            availableX = insets.left;
            availableY = insets.top;
            availableWidth = bounds.width - insets.left - insets.right;
            availableHeight = bounds.height - insets.top - insets.bottom;

            // TODO: test on other operating systems
            if (OS.CURRENT == OS.WINDOWS) {
                Frame frame = new Frame();
                frame.setLayout(null);
                frame.pack();
                Insets frameInsets = frame.getInsets();
                frameDecorationLeft = frameInsets.left;
                frameDecorationTop = frameInsets.top;
                frameDecorationRight = frameInsets.right;
                frameDecorationBottom = frameInsets.bottom;
                frame.dispose();
            } else {
                frameDecorationLeft = frameDecorationTop = frameDecorationRight = frameDecorationBottom = 0;
            }
        }

        int frameWidth = ACCEPTABLE_RESOLUTIONS[0][0], frameHeight = ACCEPTABLE_RESOLUTIONS[0][1];
        for (int i = 1; i < ACCEPTABLE_RESOLUTIONS.length; i++) {
            if (ACCEPTABLE_RESOLUTIONS[i][0] > availableWidth || ACCEPTABLE_RESOLUTIONS[i][1] > availableHeight)
                break;
            frameWidth = ACCEPTABLE_RESOLUTIONS[i][0];
            frameHeight = ACCEPTABLE_RESOLUTIONS[i][1];
        }

        this.title = title;

        // Create the window
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        pointer = glfwCreateWindow(frameWidth - frameDecorationLeft - frameDecorationRight,
                frameHeight - frameDecorationTop - frameDecorationBottom, title, NULL, NULL);

        if (pointer == NULL)
            throw new IllegalStateException("Failed to create GLFW window");

        // get framebuffer size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(pointer, pWidth, pHeight);
            this.width = pWidth.get(0);
            this.height = pHeight.get(0);
        }

        // Add keyboard handler
        glfwSetKeyCallback(pointer, (window, key, scancode, action, mods) -> {
            if (action == GLFW_PRESS)
                keyPressCallback.accept(key);
            else if (action == GLFW_RELEASE)
                keyReleaseCallback.accept(key);
            else if (action == GLFW_REPEAT)
                keyRepeatCallback.accept(key);
        });

        glfwSetCharCallback(pointer, (window, codepoint) -> {
            charTypedCallback.accept(codepoint);
        });

        // Add mouse handler
        glfwSetMouseButtonCallback(pointer, (window, button, action, mods) -> {
            if (action == GLFW_PRESS)
                mousePressCallback.accept(button);
            else if (action == GLFW_RELEASE)
                mouseReleaseCallback.accept(button);
        });

        // Add scroll handler
        glfwSetScrollCallback(pointer, (window, xoffset, yoffset) -> {
            mouseScrollCallback.accept(yoffset);
        });

        // Add window size callback
        glfwSetFramebufferSizeCallback(pointer, (window, newWidth, newHeight) -> {
            this.width = newWidth;
            this.height = newHeight;
            resizeCallback.run();
            resized = true;
        });

        // Add cursor pos callback
        glfwSetCursorPosCallback(pointer, (window, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;
        });
        glfwSetCursorEnterCallback(pointer, (window, entered) -> {
            mouseInside = entered;
        });


        // Center the window on the screen
        glfwSetWindowPos(pointer,
                availableX + frameDecorationLeft + (availableWidth - frameWidth) / 2,
                availableY + frameDecorationTop + (availableHeight - frameHeight) / 2);

        glfwSwapInterval(1);

        resized = true;
    }

    /**
     * Displays this window class on the screen
     */
    public void show() {
        glfwShowWindow(pointer);

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isFullscreen() {
        return glfwGetWindowMonitor(pointer) != NULL;
    }

    /*
     * TODO(Joe): from the glfwPollEvents documentation:
     * On some platforms, a window move, resize or menu operation will
     * cause event processing to block. This is due to how event processing
     * is designed on those platforms. You can use the window refresh
     * callback to redraw the contents of your window when necessary during
     * such operations.
     */
    public void setFullscreen(boolean fullscreen) {
        if (fullscreen == isFullscreen())
            return;

        if (fullscreen) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer pX = stack.mallocInt(1);
                IntBuffer pY = stack.mallocInt(1);
                glfwGetWindowPos(pointer, pX, pY);
                IntBuffer pWidth = stack.mallocInt(1);
                IntBuffer pHeight = stack.mallocInt(1);
                glfwGetWindowSize(pointer, pWidth, pHeight);
                windowedX = pX.get(0);
                windowedY = pY.get(0);
                windowedWidth = pWidth.get(0);
                windowedHeight = pHeight.get(0);
            }
            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            glfwSetWindowMonitor(pointer, monitor, 0, 0, vidMode.width(), vidMode.height(), vidMode.refreshRate());
        } else {
            glfwSetWindowMonitor(pointer, NULL, windowedX, windowedY, windowedWidth, windowedHeight, GLFW_DONT_CARE);
        }
    }

    public void setKeyPressCallback(IntConsumer keyPressCallback) {
        this.keyPressCallback = keyPressCallback;
    }

    public void setKeyReleaseCallback(IntConsumer keyReleaseCallback) {
        this.keyReleaseCallback = keyReleaseCallback;
    }

    public void setKeyRepeatCallback(IntConsumer keyRepeatCallback) {
        this.keyRepeatCallback = keyRepeatCallback;
    }

    public void setCharTypedCallback(IntConsumer charTypedCallback) {
        this.charTypedCallback = charTypedCallback;
    }

    public void setMousePressCallback(IntConsumer mousePressCallback) {
        this.mousePressCallback = mousePressCallback;
    }

    public void setMouseReleaseCallback(IntConsumer mouseReleaseCallback) {
        this.mouseReleaseCallback = mouseReleaseCallback;
    }

    public void setMouseScrollCallback(DoubleConsumer mouseScrollCallback) {
        this.mouseScrollCallback = mouseScrollCallback;
    }

    public void setResizeCallback(Runnable callback) {
        this.resizeCallback = callback;
    }

    /**
     * Polls whether the window was resized in the last frame, then sets the flag to false
     */
    public boolean isResized() {
        boolean ret = resized;
        resized = false;
        return ret;
    }

    public double getMouseX() {
        return mouseX;
    }

    public double getMouseY() {
        return mouseY;
    }

    public void setCursorPos(double mouseX, double mouseY) {
        glfwSetCursorPos(pointer, mouseX, mouseY);
    }

    public boolean isMouseInside() {
        return mouseInside;
    }

    public boolean isKeyDown(int keyCode) {
        return glfwGetKey(pointer, keyCode) == GLFW_PRESS;
    }

    public boolean isShiftKeyDown() {
        return isKeyDown(GLFW_KEY_LEFT_SHIFT) || isKeyDown(GLFW_KEY_RIGHT_SHIFT);
    }

    public boolean isCtrlKeyDown() {
        return isKeyDown(GLFW_KEY_LEFT_CONTROL) || isKeyDown(GLFW_KEY_RIGHT_CONTROL)
                || (OS.CURRENT == OS.MACOS && (isKeyDown(GLFW_KEY_LEFT_SUPER) || isKeyDown(GLFW_KEY_RIGHT_SUPER)));
    }

    public boolean isAltKeyDown() {
        return isKeyDown(GLFW_KEY_LEFT_ALT) || isKeyDown(GLFW_KEY_RIGHT_ALT);
    }

    public boolean isMouseButtonDown(int mouseButton) {
        return glfwGetMouseButton(pointer, mouseButton) == GLFW_PRESS;
    }

    public void showCursor() {
        glfwSetInputMode(pointer, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    public void hideCursor() {
        glfwSetInputMode(pointer, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public void disableCursor() {
        glfwSetInputMode(pointer, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    /**
     * Returns the current aspect ratio of the window
     *
     * @return A float with value Width/Height
     */
    public float getAspectRatio() {
        return (float) width / height;

    }

    public void makeContextCurrent() {
        glfwMakeContextCurrent(pointer);
    }

    public boolean shouldClose() {
        return glfwWindowShouldClose(pointer);
    }

    public void endFrame() {
        glfwSwapBuffers(pointer);
    }


    public void free() {
        Callbacks.glfwFreeCallbacks(pointer);
        glfwDestroyWindow(pointer);
    }

    public void close() {
        glfwSetWindowShouldClose(pointer, true);
    }
}