package astechzgo.luminescent.sound;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.net.URL;
import java.util.HashMap;

import astechzgo.luminescent.utils.SystemUtils;
import de.cuina.fireandfuel.CodecJLayerMP3;
import paulscode.sound.FilenameURL;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.Source;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {

	private SoundSystem mySoundSystem;
	
	private HashMap<String, Source> sourceTypeMap = new HashMap<String, Source>();

	public SoundManager() {
		boolean aLCompatible = SoundSystem
				.libraryCompatible(LibraryLWJGLOpenAL.class);

		Class<? extends Library> libraryType;
		try {
			if (aLCompatible) {
				libraryType = LibraryLWJGLOpenAL.class; // OpenAL
				mySoundSystem = new SoundSystem(libraryType);
			} else {
				libraryType = Library.class; // "No Sound, Silent Mode"
				mySoundSystem = new SoundSystem(libraryType);
			}
		} catch (SoundSystemException e) {

			// Shouldn’t happen, but it is best to prepare for anything
			e.printStackTrace();
			return;
		}

		try {
		    try {
		        SoundSystemConfig.setSoundFilesPackage(getResourceAsURL("sounds/").toString().split("\\!")[1].replaceFirst("/", ""));
		    }
		    catch(IndexOutOfBoundsException e) {
		        SoundSystemConfig.setSoundFilesPackage("");
		    }
		    
		    SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
		    SoundSystemConfig.PREFIX_URL = "^[fF][iI][lL][eE]:/.*";
		} catch (SoundSystemException e) {
			System.err.println("error linking with the CodecWav plug-in");
		}
	}

	public SoundSystem getSoundSystem() {
		return mySoundSystem;
	}
	
	public void loadSound(String s, boolean loop) {
		String oldS = s;
		
		String filename = s.replaceAll("\\.", "/") + ".mp3";
		
		URL resourceLoc = getResourceAsURL("sounds/" + filename);
		
		if(SystemUtils.isJar())
		    filename = resourceLoc.toString();
		
		boolean priority = false;
		float x = 0;
		float y = 0;
		float z = 0;
		int aModel = SoundSystemConfig.ATTENUATION_ROLLOFF;
		float rFactor = SoundSystemConfig.getDefaultRolloff();
		newSource(priority, oldS, filename, loop, x, y, z, aModel, rFactor);
	}
	
	public void newSource(boolean priority, String sourcename, String filename, boolean toLoop, float x, float y, float z, int attmodel, float distOrRoll) {
		mySoundSystem.newSource(priority, sourcename, filename, toLoop, x, y, z, attmodel, distOrRoll);
		
		sourceTypeMap.put( sourcename,
                new Source( priority, false, toLoop, sourcename,
                        new FilenameURL(filename), null, x, y, z,
                        attmodel, distOrRoll, false ));
	}
	
	public void addUniqueSource(String sourcename, String uniquename) {
		Source sourceType = sourceTypeMap.get(sourcename);
		
		if(sourceType == null)
			return;
		
		mySoundSystem.newSource(sourceType.priority, uniquename, sourceType.filenameURL.getURL(), sourceType.filenameURL.getFilename(), sourceType.toLoop, sourceType.position.x, sourceType.position.y, sourceType.position.z, sourceType.attModel, sourceType.distOrRoll);
	}
}
