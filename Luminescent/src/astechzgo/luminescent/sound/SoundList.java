package astechzgo.luminescent.sound;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SoundList {
	private static List<String> sounds;
	
	public static void initSoundList(SoundManager s) {
		sounds = getSoundsForPackage();
		for(String f : sounds) {
			String sourceName = f;
			s.loadSound(sourceName, false);
		}
	}
	
	private static List<String> getSoundsForPackage() {
		List<String> names = new ArrayList<String>();
		
		InputStream in = new SoundList().getClass().getResourceAsStream("/resources/sounds/SoundList.txt");
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
}
