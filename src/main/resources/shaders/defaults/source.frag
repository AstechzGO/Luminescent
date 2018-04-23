#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 2) uniform sampler2D texSampler;

layout(location = 0) in vec4 fragColor;
layout(location = 1) in vec2 fragTexCoord;

layout(location = 0) out vec4 outColor;

void main() {
	vec4 texture = texture(texSampler, fragTexCoord);
	
	float r = 200.0f;
	float centrex = 424.0f;
	float centrey = 238.5f;
	
	float x = gl_FragCoord.x - centrex;
	float y = gl_FragCoord.y - centrey;
	float distance = min(sqrt((x*x)+(y*y)), r);
	outColor = vec4((texture.rgb + fragColor.rgb * (1 - texture.a)) * (sqrt((r-distance)/r)), texture.a + fragColor.a * (1 - texture.a));
}