package render.shader;

public class LightArrayUniform<T> extends Uniform<T[]> {

    private Uniform<Integer> numberOfLights;
    private Uniform<T>[] lights;

    public LightArrayUniform(Uniform<Integer> lightCount,Uniform<T>[] lights){
        numberOfLights = lightCount;
        this.lights = lights;
    }

    @Override
    public void set(T[] lights) {
        //Makes certain that there will be no overflow of lights being passed to the shader
        int lightsToPass = Math.min(lights.length, this.lights.length);

        numberOfLights.set(lightsToPass);
        for(int i = 0; i < lightsToPass; i++){
            this.lights[i].set(lights[i]);
        }
    }

}
