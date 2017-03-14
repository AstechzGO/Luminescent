package astechzgo.luminescent.shader;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;

public class ShaderList {
	
	private static List<Shader> shaderList = new ArrayList<Shader>();
	
	public static void initShaderList() {
		List<String> shaders = getShadersForPackage();
		
		for(String f : shaders) {
			String shaderName = f;
			int shaderType = getShaderTypeValue(Integer.parseInt(shaderName.substring(0, 1)));
			
			shaderList.add(new Shader(shaderName.substring(1), shaderType));
		}
	}
	
	private static List<String> getShadersForPackage() {
		List<String> names = new ArrayList<String>();
		
		InputStream in = null;
		
        try {
            in = getResourceAsURL("shaders/ShaderList.txt").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		String line = "";
		try {
			while((line = input.readLine()) != null) {
				if(line.startsWith("#")) {
					names.add(line.replaceFirst("#", ""));
				}
			}
			
			return names;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return names;
	}
	
	public static Shader findShader(String shaderName) {
		for(Shader s : shaderList) {
			if(s.getName().equals(shaderName)) {
				return s;
			}
		}
		return null;
	}
	
	private static int getShaderTypeValue(int shaderTypeIdx) {
		switch(shaderTypeIdx) {
			case 0:
				return GL20.GL_VERTEX_SHADER;
			case 1:
				return GL20.GL_FRAGMENT_SHADER;
			case 2:
				return GL32.GL_GEOMETRY_SHADER;
			case 3:
				return GL40.GL_TESS_EVALUATION_SHADER ;
			case 4:
				return GL40.GL_TESS_EVALUATION_SHADER;
			default:
				throw new IllegalArgumentException("Unknown shader type");
		}
	}
}
