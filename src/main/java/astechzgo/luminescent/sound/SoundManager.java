package astechzgo.luminescent.sound;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Objects;

import astechzgo.luminescent.utils.SystemUtils;
import org.lwjgl.openal.*;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.libc.LibCStdlib;
public class SoundManager {

	private final HashMap<String, Source> sourceTypeMap = new HashMap<>();
	private final HashMap<String, Source> sources = new HashMap<>();

	private final long device, context;

	public SoundManager() {
		String defaultDeviceName = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
		device = ALC10.alcOpenDevice(defaultDeviceName);

		int[] attributes = {0};
		context = ALC10.alcCreateContext(device, attributes);
		ALC10.alcMakeContextCurrent(context);

		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

		if(alCapabilities.OpenAL10) {

		}
	}
	
	public void loadSound(String s, boolean loop) {

		String filename = s.replaceAll("\\.", "/") + ".ogg";
		
		URL resourceLoc = getResourceAsURL("sounds/" + filename);

		ByteBuffer data = null;
		try {
			byte[] raw = resourceLoc.openStream().readAllBytes();
			data = MemoryUtil.memAlloc(raw.length);
			data.put(raw);
			data.flip();
		} catch (IOException e) {
			e.printStackTrace();
		}

		ShortBuffer rawAudioBuffer;

		int channels;
		int sampleRate;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			// Allocate space to store return information from the function
			IntBuffer channelsBuffer   = stack.mallocInt(1);
			IntBuffer sampleRateBuffer = stack.mallocInt(1);

			rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(data, channelsBuffer, sampleRateBuffer);
			MemoryUtil.memFree(data);

			// Retreive the extra information that was stored in the buffers by the function
			channels = channelsBuffer.get(0);
			sampleRate = sampleRateBuffer.get(0);
		}

		// Find the correct OpenAL format
		int format = -1;
		if (channels == 1) {
			format = AL10.AL_FORMAT_MONO16;
		} else if (channels == 2) {
			format = AL10.AL_FORMAT_STEREO16;
		}

		// Request space for the buffer
		int bufferPointer = AL10.alGenBuffers();

		// Send the data to OpenAL
		AL10.alBufferData(bufferPointer, format, Objects.requireNonNull(rawAudioBuffer), sampleRate);

		// Free the memory allocated by STB
		LibCStdlib.free(rawAudioBuffer);

		newSource(s, bufferPointer);
	}
	
	public void newSource(String sourcename, int bufferPointer) {
		// Request a source
		int sourcePointer = AL10.alGenSources();

		// Assign the sound we just loaded to the source
		AL10.alSourcei(sourcePointer, AL10.AL_BUFFER, bufferPointer);

		Source source = new Source(bufferPointer, sourcePointer);
		sourceTypeMap.put(sourcename, source);
		sources.put(sourcename, source);
	}
	
	public void addUniqueSource(String sourcename, String uniquename) {
		Source sourceType = sourceTypeMap.get(sourcename);
		
		if(sourceType == null)
			return;

		// Request a source
		int sourcePointer = AL10.alGenSources();

		// Assign the sound we just loaded to the source
		AL10.alSourcei(sourcePointer, AL10.AL_BUFFER, sourceType.getBufferPointer());

		sources.put(uniquename, new Source(sourceType.getBufferPointer(), sourcePointer));
	}

	public void play(String uniqueName) {
		Source source = sources.get(uniqueName);

		if(source != null) {
			AL10.alSourcePlay(source.getSourcePointer());
		}
	}

	public void pause(String uniqueName) {
		Source source = sources.get(uniqueName);

		if(source != null) {
			AL10.alSourcePause(source.getSourcePointer());
		}
	}

	public void stop(String uniqueName) {
		Source source = sources.get(uniqueName);

		if(source != null) {
			AL10.alSourceStop(source.getSourcePointer());
		}
	}

	public boolean playing(String uniqueName) {
		Source source = sources.get(uniqueName);

		if(source != null) {
			return AL10.alGetSourcei(source.getSourcePointer(), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
		}
		else {
			return false;
		}
	}

	public void unloadSound(String uniqueName) {
		Source source = sources.get(uniqueName);

		if(source != null) {
			AL10.alDeleteSources(source.getSourcePointer());
		}
	}

	public void cleanup() {
		for(Source source : sourceTypeMap.values()) {
			AL10.alDeleteSources(source.getSourcePointer());
			AL10.alDeleteBuffers(source.getBufferPointer());
		}

		ALC10.alcDestroyContext(context);
		ALC10.alcCloseDevice(device);
	}
}
