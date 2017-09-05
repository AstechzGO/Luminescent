package astechzgo.luminescent.shader;

import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.utils.SystemUtils;

public class Shader {

	private final String name;
	private final byte[] shaderCode;

	private final int shaderType;
	private final long shaderAddress;

	public Shader(String name, int shaderType) {
		this.name = name;
		this.shaderType = shaderType;

		byte[] value = null;
		try {
			value = getShaderCode(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
		shaderCode = value;
		shaderAddress = Vulkan.getShaderHandle(value);
	}

	private byte[] getShaderCode(String shaderLoc) throws Exception {
		shaderLoc = shaderLoc.replaceAll("\\.", "/");
		
	    return SystemUtils.readFile("shaders/" + shaderLoc + ".spv");
	}

	public String getName() {
		return name;
	}

	public byte[] getShaderCode() {
		return shaderCode;
	}

	public int getShaderType() {
		return shaderType;
	}

	public long getShaderAddress() {
		return shaderAddress;
	}
}
