package astechzgo.luminescent.shader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import astechzgo.luminescent.utils.DisplayUtils;
 
public class ShaderProgram 
{
	// OpenGL handle that will point to the executable shader program
	// that can later be used for rendering
	private final int programId;
 
	private final Shader vertexShader;
	private final Shader fragmentShader;
	
	public static final String TRANS_MATRIX = "u_projTrans";
	
	public ShaderProgram(Shader vertexShader, Shader fragmentShader) {
		this.vertexShader = vertexShader;
		this.fragmentShader = fragmentShader;
		
		// Create the shader program. If OK, create vertex and fragment shaders
		programId = GL20.glCreateProgram();
 
		// Attach the compiled shaders to the program
		GL20.glAttachShader(programId, vertexShader.getShaderHandle());
		GL20.glAttachShader(programId, fragmentShader.getShaderHandle());
 
		// Position information will be attribute 0
		GL20.glBindAttribLocation(programId, 0, "a_position");
		// Color information will be attribute 1
		GL20.glBindAttribLocation(programId, 1, "a_color");
		// Textute information will be attribute 2
		GL20.glBindAttribLocation(programId, 2, "a_texCoord0");
		
		// Link the program
		GL20.glLinkProgram(programId);
 
		// Validate linking
		if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) 
		{
			throw new RuntimeException("could not link shader. Reason: " + GL20.glGetProgramInfoLog(programId, 1000));
		}
 
		// Perform general validation that the program is usable
		GL20.glValidateProgram(programId);
 
		if (GL20.glGetProgrami(programId, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE)
		{
			throw new RuntimeException("could not validate shader. Reason: " + GL20.glGetProgramInfoLog(programId, 1000));            
		}		
		
		System.out.println(GL20.glGetProgramInfoLog(programId, 1000));
	}

	public void applyShader() {
		GL20.glUseProgram(getProgramId());
	}
	
	public void withdrawShader() {
		GL20.glUseProgram(0);
	}
	
	public int getProgramId() {
		return programId;
	}    
	
	public Shader getVertexShader() {
		return vertexShader;
	}
	
	public Shader getFragmentShader() {
		return fragmentShader;
	}
	
	public void updateTransMatrix() {
		int location = GL20.glGetUniformLocation(programId, TRANS_MATRIX);
		float[] value = new float[] {
				2.0f / DisplayUtils.getDisplayWidth(), 0.0f, 0.0f, -1.0f,
				0.0f, 2.0f / DisplayUtils.getDisplayHeight(), 0.0f, -1.0f,
				0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f
		};
		
		GL20.glUniformMatrix4fv(location, true, value);
	}
}