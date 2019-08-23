package util;

public class MathUtil {

    public static int clamp(int val, int min, int max) {
        return val < min ? min : val > max ? max : val;
    }
    
    public static long clamp(long val, long min, long max) {
        return val < min ? min : val > max ? max : val;
    }
    
    public static float clamp(float val, float min, float max) {
        return val < min ? min : val > max ? max : val;
    }
    
    public static double clamp(double val, double min, double max) {
        return val < min ? min : val > max ? max : val;
    }

    public static int lerp(int a, int b, float delta) {
        return a + (int) ((b - a) * delta);
    }

    public static float lerp(float a, float b, float delta) {
        return a + (b - a) * delta;
    }

    public static double lerp(double a, double b, float delta) {
        return a + (b - a) * delta;
    }

    public static float lerpAngle(float a, float b, float delta) {
        return a + angleDifference(a, b) * delta;
    }

    public static double lerpAngle(double a, double b, float delta) {
        return a + angleDifference(a, b) * delta;
    }

    public static float angleDifference(float a, float b) {
        float da = (b - a) % Mathf.PI * 2;
        return 2 * da % Mathf.PI * 2 - da;
    }

    public static double angleDifference(double a, double b) {
        double da = (b - a) % (Math.PI * 2);
        return 2 * da % (Math.PI * 2) - da;
    }

    public static float wrapAngle(float theta) {
        theta %= Mathf.PI * 2;
        if (theta >= Mathf.PI)
            theta -= Mathf.PI * 2;
        else if (theta < -Mathf.PI)
            theta += Mathf.PI * 2;
        return theta;
    }

    public static double wrapAngle(double theta) {
        theta %= Math.PI * 2;
        if (theta >= Math.PI)
            theta -= Math.PI * 2;
        else if (theta < -Math.PI)
            theta += Math.PI * 2;
        return theta;
    }

    public static boolean approxEqual(float a, float b) {
        return org.joml.Math.abs(b - a) < 0.0001f;
    }

    public static boolean approxEqual(double a, double b) {
        return org.joml.Math.abs(b - a) < 0.0001;
    }
    
}
