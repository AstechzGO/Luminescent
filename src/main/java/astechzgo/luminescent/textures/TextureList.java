package astechzgo.luminescent.textures;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TextureList {
	private static List<Texture> textures = new ArrayList<>();
	
	public static void loadTextures() {
		InputStream in = null;
		
        try {
            in = getResourceAsURL("textures/TextureList.txt").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
		
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		String line = "";
		try {
			while((line = input.readLine()) != null) {
				if(line.startsWith("#")) {
					// Creating a texture adds it to the texture list
					new Texture(line.replaceFirst("#", ""));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Texture findTexture(String textureName) {
		for(Texture t : textures) {
			if(t.getName().equals(textureName)) {
				return t;
			}
		}
		return null;
	}
	
	public static void setTextures(List<Texture> textures) {
		TextureList.textures = textures;
	}
	
	public static void addTextures(List<Texture> textures) {
		TextureList.textures.addAll(textures);
	}
	
	public static void addTexture(Texture texture) {
	    TextureList.textures.add(texture);
	}
	
	public static void removeTexture(Texture texture) {
	    TextureList.textures.remove(texture);
	}

	public static void cleanup() {
		for(Texture texture : textures) {
			texture.dispose();
		}
	}
}
