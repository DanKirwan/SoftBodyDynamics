package util;

import org.joml.*;

import java.lang.Math;

public class VectorUtil {
    public static Matrix3f zeroMatrix3f() {
        return new Matrix3f(
                0, 0, 0,
                0, 0, 0,
                0, 0, 0
        );
    }

    public static Matrix4f zeroMatrix4f() {
        return new Matrix4f(
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, 0, 0
        );
    }

    /**
     * outputs the maximum possible point in any axis from two vector inputs
     */
    public static void componentWiseMax(Vector3f current, Vector3fc comp){
        current.set(Math.max(current.x, comp.x()), Math.max(current.y, comp.y()), Math.max(current.z, comp.z()));
    }

    /**
     * outputs the minimum possible point in any axis from two vector inputs
     */
    public static void componentWiseMin(Vector3f current, Vector3fc comp) {
        current.set(Math.min(current.x, comp.x()), Math.min(current.y, comp.y()), Math.min(current.z, comp.z()));
    }

    public static Vector3f matrixTransform(Vector3f position, Matrix4f transformation) {
        Vector4f pos = new Vector4f(position.x,position.y,position.z,1);
        pos.mul(transformation);
        position.set(pos.x,pos.y,pos.z);
        return position;
    }

    public static Vector3f getNormal(Vector3f a, Vector3f b, Vector3f c) {
        return b.sub(a, new Vector3f())
                .cross(c.x - a.x, c.y - a.y, c.z - a.z)
                .normalize();
    }

    public static Quaternionf eulerToQuaternion(Vector3fc euler) {
        return eulerToQuaternion(euler.y(), euler.x(), euler.z());
    }

    public static Quaternionf eulerToQuaternion(float yaw, float pitch, float roll) {
        Quaternionf q = new Quaternionf();
        q.rotateLocalX(-roll); //because :thonk:
        q.rotateLocalZ(pitch);
        q.rotateLocalY(yaw);
        return q;
    }

    public static Vector3f quaternionToEuler(Quaternionfc quat) {
        float qw = quat.w();
        float qx = quat.x();
        float qy = quat.y();
        float qz = quat.z();
        float qwqw = qw*qw;
        float qwqx = qw*qx;
        float qwqy = qw*qy;
        float qwqz = qw*qz;
        float qxqx = qx*qx;
        float qxqy = qx*qy;
        float qxqz = qx*qz;
        float qyqy = qy*qy;
        float qyqz = qy*qz;
        float qzqz = qz*qz;

        // Let mat = the 3x3 rotation matrix equivalent to quat

        // Let y = yaw, p = pitch, r = roll

        // Let Y be the rotation matrix due to yaw:
        //     [ cosy     0    siny ]
        // Y = [ 0        1    0    ]
        //     [ -siny    0    cosy ]

        // Let P be the rotation matrix due to pitch:
        //     [ cosp    -sinp    0 ]
        // P = [ sinp    cosp     0 ]
        //     [ 0       0        1 ]

        // Let R be the rotation matrix due to roll:
        //     [ 1    0        0    ]
        // R = [ 0    cosr     sinr ]
        //     [ 0    -sinr    cosr ]

        // Let M' be the inverse of M

        // We first rotate a vector v by yaw:
        // v1 = Yv
        // We then rotate by pitch, but in local coordinates, so we need to do a change of basis:
        // v2 = YPY' Y v
        // Simplified:
        // v2 = YPv
        // We then rotate by roll, but in local coordinates, so we need to do a change of basis:
        // v3 = YP R (YP)' YP v
        // Simplified:
        // v3 = YPRv

        // So mat = YPR:
        //       [ cosy cosp     -cosy sinp cosr - siny sinr    siny cosr - cosy sinp sinr ]
        // mat = [ sinp          cosp cosr                      cosp sinr                  ]
        //       [ -siny cosp    siny sinp cosr - cosy sinr     cosy cosr + siny sinp sinr ]

        // We assume that -pi/2 <= p <= pi/2, and we can read sinp directly from the matrix.
        // This gives us a unique value for p
        float sinp = qxqy + qxqy + qwqz + qwqz; // m01
        if (MathUtil.approxEqual(Math.abs(sinp), 1)) {
            // The vector is either facing directly up or directly down.
            // In this special case, we are assuming roll = 0, as all
            // such configurations are reachable by just changing yaw and pitch.
            float pitch = sinp > 0 ? Mathf.PI/2 : -Mathf.PI/2;

            float siny = qxqz + qxqz + qwqy + qwqy; // m20
            float cosy = qzqz - qyqy - qxqx + qwqw; // m22
            float yaw = Mathf.atan2(siny, cosy);
            return new Vector3f(pitch, yaw, 0);
        }

        float cosp = Mathf.sqrt(1 - sinp);
        float pitch = Mathf.asin(sinp);

        float cosy = (qwqw + qxqx - qzqz - qyqy) / cosp; // m00 / cosp
        float siny = -(qxqz + qxqz - qwqy - qwqy) / cosp; // m02 / cosp
        float yaw = Mathf.atan2(siny, cosy);

        float cosr = (qyqy + qwqw - qzqz - qxqx) / cosp; // m11 / cosp
        float sinr = (qyqz + qyqz - qwqx - qwqx) / cosp; // m21 / cosp
        float roll = Mathf.atan2(sinr, cosr);

        return new Vector3f(pitch, yaw, roll);
    }

    /**
     * Temporary method while it's fixed in JOML
     */
    public static Matrix3f mulLocal(Matrix3f right, Matrix3fc left) {
        return mulLocal(right, left, right);
    }

    /**
     * Temporary method while it's fixed in JOML
     */
    public static Matrix3f mulLocal(Matrix3fc right, Matrix3fc left, Matrix3f dest) {
        return left.mul(right, dest);
    }

    // Debug methods

    public static String matrixToWolfram(Matrix3f mat) {
        return "{{" + mat.m00() + ", " + mat.m10() + ", " + mat.m20() + "}, " +
                "{" + mat.m01() + ", " + mat.m11() + ", " + mat.m21() + "}, " +
                "{" + mat.m02() + ", " + mat.m12() + ", " + mat.m22() + "}}";
    }

    public static String matrixToWolfram(Matrix4f mat) {
        return "{{" + mat.m00() + ", " + mat.m10() + ", " + mat.m20() + ", " + mat.m30() + "}, " +
                "{" + mat.m01() + ", " + mat.m11() + ", " + mat.m21() + ", " + mat.m31() + "}, " +
                "{" + mat.m02() + ", " + mat.m12() + ", " + mat.m22() + ", " + mat.m23() + "}, " +
                "{" + mat.m03() + ", " + mat.m13() + ", " + mat.m23() + ", " + mat.m33() + "}}";
    }
}
