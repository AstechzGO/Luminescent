package astechzgo.luminescent.shader;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.vulkan.VK10;

public class ShaderList {
	
	private static final List<Shader> shaderList = new ArrayList<>();
	
	public static void initShaderList() {
		List<String> shaders = getShadersForPackage();
		
		for(String f : shaders) {
			int shaderType = getShaderTypeValue(Integer.parseInt(f.substring(0, 1)));
			
			shaderList.add(new Shader(f.substring(1), shaderType));
		}
	}
	
	private static List<String> getShadersForPackage() {
		List<String> names = new ArrayList<>();
		
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
				return VK10.VK_SHADER_STAGE_VERTEX_BIT;
			case 1:
				return VK10.VK_SHADER_STAGE_TESSELLATION_CONTROL_BIT;
			case 2:
				return VK10.VK_SHADER_STAGE_TESSELLATION_EVALUATION_BIT;
			case 3:
				return VK10.VK_SHADER_STAGE_GEOMETRY_BIT;
			case 4:
				return VK10.VK_SHADER_STAGE_FRAGMENT_BIT;
            case 5:
                return VK10.VK_SHADER_STAGE_COMPUTE_BIT;
            case 6:
                return VK10.VK_SHADER_STAGE_ALL_GRAPHICS;
            case 7:
                return VK10.VK_SHADER_STAGE_ALL;
			default:
				throw new IllegalArgumentException("Unknown shader type");
		}
	}
}
