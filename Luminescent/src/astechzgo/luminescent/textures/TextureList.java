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
	
	public static final List<Texture> TEXTURES = new ArrayList<Texture>();
	
	public static void initTextureList() {
		getAllTextures();
		for(String f : slickTextures) {
			TEXTURES.add(new Texture(f, true));
		}
		
		for(String f : nonSlickTextures) {
			TEXTURES.add(new Texture(f, false));
		}
	}
	
	private static void getAllTextures() {
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
		for(Texture t : TEXTURES) {
			if(t.getName().equals(textureName)) {
				return t;
			}
		}
		return null;
	}
}
