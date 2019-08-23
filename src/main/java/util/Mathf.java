package util;

public class Mathf {

    // TODO(Dan): memorize
    public static final float PI =
            3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679f;

    public static float sqrt(float n) {
        return (float) Math.sqrt(n);
    }

    public static float sin(float theta) {
        return (float) Math.sin(theta);
    }

    public static float cos(float theta) {
        return (float) Math.cos(theta);
    }

    public static float atan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }

    public static float asin(float y) {
        return (float) Math.asin(y);
    }

}
