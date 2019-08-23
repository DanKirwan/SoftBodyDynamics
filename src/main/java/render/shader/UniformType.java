package render.shader;

import render.render3D.light.*;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

@FunctionalInterface
public interface UniformType<T> {

    // TODO (Joe): add more uniform types
    UniformType<Boolean> BOOLEAN = (name, program) -> new BooleanUniform(program.getUniformLocation(name));
    UniformType<Integer> INTEGER = (name, program) -> new IntegerUniform(program.getUniformLocation(name));
    UniformType<Float> FLOAT = (name, program) -> new FloatUniform(program.getUniformLocation(name));
    UniformType<Vector3f> VECTOR3F = (name, program) -> new Vector3fUniform(program.getUniformLocation(name));
    UniformType<Vector4f> VECTOR4F = (name, program) -> new Vector4fUniform(program.getUniformLocation(name));
    UniformType<Matrix3f> MATRIX3F = (name, program) -> new Matrix3fUniform(program.getUniformLocation(name));
    UniformType<Matrix4f> MATRIX4F = (name, program) -> new Matrix4fUniform(program.getUniformLocation(name));
    UniformType<Attenuation> ATTENUATION = (name, program) -> new AttenuationUniform(
            program.createUniform(name + ".constant", FLOAT),
            program.createUniform(name + ".linear", FLOAT),
            program.createUniform(name + ".exponent", FLOAT)
    );
    UniformType<PointLight> POINT_LIGHT = (name, program) -> new PointLightUniform(
            program.createUniform(name + ".colour", VECTOR3F),
            program.createUniform(name + ".position", VECTOR3F),
            program.createUniform(name + ".intensity", FLOAT),
            program.createUniform(name + ".att", ATTENUATION)
    );


    UniformType<PointLight[]> POINT_LIGHT_ARRAY = (name, program) -> {

        Uniform<PointLight>[] uniforms = new Uniform[8];
        for (int i = 0; i < uniforms.length; i++)
            uniforms[i] = program.createUniform(name + "[" + i + "]",POINT_LIGHT);

        return new LightArrayUniform<>(program.createUniform(name + "Count",INTEGER),uniforms);
    };



    Uniform<T> createUniform(String name, ShaderProgram program);

}
