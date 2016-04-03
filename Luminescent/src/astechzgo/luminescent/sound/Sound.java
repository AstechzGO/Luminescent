package astechzgo.luminescent.sound;

import java.rmi.server.UID;

public class Sound {

	private static final SoundManager soundManager = new SoundManager();
	
	public static void init() {
		SoundList.initSoundList(soundManager);
	}
	
	private final String sourcename;
	private final String uniquename;
	
	boolean destroyed = false;
	
	public Sound(String sourcename) {
		this.sourcename = sourcename;
		this.uniquename = getUniqueName(sourcename);
	}
	
	private String getUniqueName(String sourcename) {
		String uniquename = sourcename + new UID();
		
		soundManager.addUniqueSource(sourcename, uniquename);
		
		return uniquename;
	}
	
	public String getName() {
		return sourcename;
	}
	
	public void play() {
		if(destroyed) {
			System.err.println("Can't play destroyed object!");
			return;
		}
		
		soundManager.getSoundSystem().play(uniquename);
	}
	
	public void stop() {
		if(destroyed) {
			System.err.println("Can't stop destroyed object!");
			return;
		}
		
		if(soundManager.getSoundSystem().playing(uniquename))
			soundManager.getSoundSystem().stop(uniquename);
	}
	
	public void restart() {
		stop();
		play();
	}
	
	public void pause() {
		if(destroyed) {
			System.err.println("Can't pause destroyed object!");
			return;
		}
		
		soundManager.getSoundSystem().pause(uniquename);
	}
	
	public void playDestroy() {
		play();
		destroy();
	}
	
	public void destroy() {
		if(destroyed) {
			System.err.println("Can't destroy destroyed object!");
			return;
		}
		
		soundManager.getSoundSystem().unloadSound(uniquename);
		destroyed = true;
	}
}
