#version 120

attribute vec3 pos;
attribute vec3 norm;

uniform mat4 modelMat;
uniform mat4 projViewMat;

varying vec3 worldPos;
varying vec3 worldNorm;




void main() {
	worldPos = (modelMat * vec4(pos,1)).xyz;
	worldNorm = (modelMat * vec4(norm,0)).xyz;
	gl_Position = projViewMat * modelMat * vec4(pos, 1.0);

}
