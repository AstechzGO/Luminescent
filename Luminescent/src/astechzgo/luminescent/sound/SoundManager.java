package astechzgo.luminescent.sound;

import de.cuina.fireandfuel.CodecJLayerMP3;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SoundManager {

	private SoundSystem mySoundSystem;

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
			SoundSystemConfig.setSoundFilesPackage("resources/sounds/");
			SoundSystemConfig.setCodec("mp3", CodecJLayerMP3.class);
		} catch (SoundSystemException e) {
			System.err.println("error linking with the CodecWav plug-in");
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				mySoundSystem.cleanup();
			}
		});
	}

	public SoundSystem getSoundSystem() {
		return mySoundSystem;
	}
	
	public void loadSound(String s, boolean loop) {
		String oldS = s;
		
		s = s.replaceAll("\\.", "/") + ".mp3";
		
		boolean priority = false;
		String filename = s;
		float x = 0;
		float y = 0;
		float z = 0;
		int aModel = SoundSystemConfig.ATTENUATION_ROLLOFF;
		float rFactor = SoundSystemConfig.getDefaultRolloff();
		mySoundSystem.newSource(priority, oldS, filename, loop, x, y, z, aModel, rFactor);
	}
}
