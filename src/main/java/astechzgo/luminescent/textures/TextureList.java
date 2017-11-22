package astechzgo.luminescent.textures;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TextureList {
	private static List<String> nonSlickTextures = new ArrayList<>();
	private static List<String> slickTextures = new ArrayList<>();
	
	private static List<Texture> textures = new ArrayList<>();
	
	public static void getAllTextures() {
		List<String> nonSlick = new ArrayList<>();
		List<String> slick = new ArrayList<>();
		
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
					slick.add(line.replaceFirst("#", ""));
				}
				else if(line.startsWith("$")) {
					nonSlick.add(line.replaceFirst("\\$", ""));
				}
			}
			
			nonSlickTextures = nonSlick;
			slickTextures = slick;
			
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
	
	public static List<String> getSlickTextureNames() {
		return slickTextures;
	}
	
	public static List<String> getNonSlickTextureNames() {
		return nonSlickTextures;
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
	
	public static void loadNonSlickTextures() {
		TextureList.getAllTextures();
		
		List<Texture> textures = new ArrayList<>();
		
		for(String f : TextureList.getNonSlickTextureNames()) {
			textures.add(new Texture(f, false));
		}
	}
	
	public static void loadSlickTextures() {
		List<Texture> textures = new ArrayList<>();
		
		for(String f : getSlickTextureNames()) {
			textures.add(new Texture(f, true));
		}
	}
	
	public static void cleanup() {
		while(textures.size() > 0) {
			textures.get(0).dispose();
		}
	}
}
