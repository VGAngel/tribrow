/*
* fragment shader template
*/
in vec3 fPosition;
void main() {
    // Set the fragment color for example to gray, alpha 1.0
    gl_FragData[0]=vec4(fPosition,1);
}

