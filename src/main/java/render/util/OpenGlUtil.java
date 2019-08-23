package render.util;

import static org.lwjgl.opengl.GL11.*;

/**
 * General utilities relating to OpenGL for use by Clockback
 */
public class OpenGlUtil {

    /**
     * Gets the number of bytes taken up by the given GL primitive type
     */
    public static int sizeof(int type) {
        switch (type) {
            case GL_BYTE:
            case GL_UNSIGNED_BYTE:
                return 1;
            case GL_SHORT:
            case GL_UNSIGNED_SHORT:
            case GL_2_BYTES:
                return 2;
            case GL_3_BYTES:
                return 3;
            case GL_INT:
            case GL_UNSIGNED_INT:
            case GL_FLOAT:
            case GL_4_BYTES:
                return 4;
            case GL_DOUBLE:
                return 8;
            default:
                throw new IllegalArgumentException("Unrecognized GL primitive type: " + type);
        }
    }

    public static void enableTransparency() {
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    public static void disableTransparency() {
        glDisable(GL_BLEND);
    }

}
