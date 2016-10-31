package astechzgo.luminescent.textures;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Animation extends Texture {
	
	private static int idx = 0;
	
	private static final Timer t;
	
	static {		
		TimerTask tt = new TimerTask() {
			@Override
			public void run() {
				updateIndex();
			}
		};
		
		t = new Timer();
		t.scheduleAtFixedRate(tt, 100, 100);
	}
	
	public static void cleanup() {
		t.cancel();
	}
	
	private static void updateIndex() {
		idx++;
	}
	
	private List<Texture> frames = new ArrayList<Texture>();	
	
	public Animation(String textureName, int count) {
		super(textureName + "$0", true);

		for(int i = 0; i < count; i++) {
			frames.add(TextureList.findTexture(textureName + "$" + i));
		}
	}
	
	@Override
	public BufferedImage getAsBufferedImage() {
		return frames.get(idx % frames.size()).getAsBufferedImage();
	}
	
	@Override
	public ByteBuffer getAsByteBuffer() {
		return frames.get(idx % frames.size()).getAsByteBuffer();
	}
	
	@Override
	public int getAsTexture() {
		return frames.get(idx % frames.size()).getAsTexture();
	}
}
