uniform mat4 g_WorldViewProjectionMatrix;
in vec3 inPosition;
varying vec3 fPosition;
/*
* vertex shader template
*/
void main() { 
    // Vertex transformation 
    gl_Position = g_WorldViewProjectionMatrix*vec4(inPosition,1); 
    fPosition=gl_Position.xyz;
}
