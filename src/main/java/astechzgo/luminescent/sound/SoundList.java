package astechzgo.luminescent.sound;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SoundList {

	public static void initSoundList(SoundManager s) {
		List<String> sounds = getSoundsForPackage();
		for(String f : sounds) {
			s.loadSound(f, false);
		}
	}
	
	private static List<String> getSoundsForPackage() {
		List<String> names = new ArrayList<>();
		
		InputStream in = null;
		
        try {
            in = getResourceAsURL("sounds/SoundList.txt").openStream();
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
}
