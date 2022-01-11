#version 450
#pragma shader_stage(fragment)

layout(binding = 2) uniform sampler2D texSampler;

layout(binding = 3) uniform LightSource {
	vec3 source[250];
} lights;

layout(location = 0) in vec4 fragColor;
layout(location = 1) in vec2 fragTexCoord;
layout(location = 2) in float fragDoLighting;

layout(location = 0) out vec4 outColor;

void main() {
	vec4 texture = texture(texSampler, fragTexCoord);
	
	float sumLight = 0;
	
	if(fragDoLighting != 0) {
		for(int i = 0; i < lights.source.length(); i++) {
			if(lights.source[i].z > 0) {
				float x = gl_FragCoord.x - lights.source[i].x;
				float y = gl_FragCoord.y - lights.source[i].y;
				float r = lights.source[i].z;
				
				float distance = min(sqrt((x*x)+(y*y)), r);
			
				sumLight += (r-distance)/r;
			}
		}
	}
	else {
		sumLight = 1;
	}
	

	outColor = vec4((texture.rgb + fragColor.rgb * (1 - texture.a)) * min(sumLight, 1), texture.a + fragColor.a * (1 - texture.a));
}