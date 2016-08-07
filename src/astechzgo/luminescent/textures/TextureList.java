package astechzgo.luminescent.textures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class TextureList {
	private static List<String> nonSlickTextures = new ArrayList<String>();
	private static List<String> slickTextures = new ArrayList<String>();
	
	private static List<Texture> textures = new ArrayList<Texture>();
	
	public static void getAllTextures() {
		List<String> nonSlick = new ArrayList<String>();
		List<String> slick = new ArrayList<String>();
		
		InputStream in = new TextureList().getClass().getResourceAsStream("/resources/textures/TextureList.txt");
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
	
	public static void loadNonSlickTextures() {
		TextureList.getAllTextures();
		
		List<Texture> textures = new ArrayList<Texture>();
		
		for(String f : TextureList.getNonSlickTextureNames()) {
			textures.add(new Texture(f, false));
		}
		
		TextureList.setTextures(textures);
	}
	
	public static void loadSlickTextures() {
		List<Texture> textures = new ArrayList<Texture>();
		
		for(String f : getSlickTextureNames()) {
			textures.add(new Texture(f, true));
		}
		
		TextureList.addTextures(textures);
	}
	
	public static void cleanup() {
		for(Texture texture : textures)
			texture.dispose();
	}
}
