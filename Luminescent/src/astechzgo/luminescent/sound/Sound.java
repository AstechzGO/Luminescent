package astechzgo.luminescent.sound;

import java.rmi.server.UID;

import paulscode.sound.SoundSystemConfig;

public class Sound {

	private static final SoundManager soundManager = new SoundManager();
	
	static {
		SoundList.initSoundList(soundManager);
	}
	
	private final String sourcename;
	private final String uniquename;
	
	public Sound(String sourcename) {
		this.sourcename = sourcename;
		this.uniquename = getUniqueName(sourcename);
	}
	
	private String getUniqueName(String sourcename) {
		// TODO: Copy just copy sound source instead of loading it twice
		String uniquename = sourcename + new UID();
		
		String filename = sourcename.replaceAll("\\.", "/") + ".mp3";
		
		boolean priority = false;
		float x = 0;
		float y = 0;
		float z = 0;
		int aModel = SoundSystemConfig.ATTENUATION_ROLLOFF;
		float rFactor = SoundSystemConfig.getDefaultRolloff();
		soundManager.getSoundSystem().newSource(priority, uniquename, filename, false, x, y, z, aModel, rFactor);
		
		return uniquename;
	}
	
	public String getName() {
		return sourcename;
	}
	
	public void play() {
		soundManager.getSoundSystem().play(uniquename);
	}
	
	public void stop() {
		if(soundManager.getSoundSystem().playing(uniquename))
			soundManager.getSoundSystem().stop(uniquename);
	}
	
	public void restart() {
		stop();
		play();
	}
	
	public void pause() {
		soundManager.getSoundSystem().pause(uniquename);
	}
}
