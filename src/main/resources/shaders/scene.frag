#version 120



varying vec3 worldPos;
varying vec3 worldNorm;
uniform vec3 lightPos;


void main() {
    vec3 toLightDir = lightPos - worldPos;
    toLightDir = normalize(toLightDir);
    float brightness = dot(toLightDir, worldNorm);
	gl_FragColor =  vec4(brightness);
}
