package astechzgo.luminescent.shader;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {

	private final String name;
	private final String source;

	private final int shaderType;
	private final int shaderHandle;

	public Shader(String name, int shaderType) {
		this.name = name;
		this.shaderType = shaderType;

		String value = null;
		try {
			value = getSource(name);
		} catch (Exception e) {
			e.printStackTrace();
		}

		source = value;

		shaderHandle = loadAndCompileShader(source, shaderType);
	}

	private String getSource(String shaderLoc) throws Exception {
		shaderLoc = shaderLoc.replaceAll("\\.", "/");
		
	    InputStream in = null;
	        
	    try {
	        in = getResourceAsURL("shaders/" + shaderLoc + ".glsl").openStream();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }

		StringBuilder vertexCode = new StringBuilder();
		String line = null;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			while ((line = reader.readLine()) != null) {
				vertexCode.append(line);
				vertexCode.append('\n');
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return vertexCode.toString();
	}

	private int loadAndCompileShader(String source, int shaderType) {
		// Will be non zero if successfully created
		int handle = GL20.glCreateShader(shaderType);

		if (handle == 0) {
			throw new RuntimeException(
					"Could not created shader of type " + shaderType + " for shader " + this.getName());
		}

		// Upload code to OpenGL and associate code with shader
		GL20.glShaderSource(handle, source);

		// Compile source code into binary
		GL20.glCompileShader(handle);

		// Acquire compilation status
		int shaderStatus = GL20.glGetShaderi(handle, GL20.GL_COMPILE_STATUS);

		// Check whether compilation was successful
		if (shaderStatus == GL11.GL_FALSE) {
			throw new IllegalStateException("Compilation error for shader [" + this.getName() + "]. Reason: "
					+ GL20.glGetShaderInfoLog(handle, 1000));
		}

		return handle;
	}

	public String getName() {
		return name;
	}

	public String getSource() {
		return source;
	}

	public int getShaderType() {
		return shaderType;
	}

	public int getShaderHandle() {
		return shaderHandle;
	}
}
