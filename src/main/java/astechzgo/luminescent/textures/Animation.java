package astechzgo.luminescent.textures;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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
	
	private final List<Texture> frames = new ArrayList<>();
	
	public Animation(String textureName, int count) {
		super(textureName, true, toCombinedImage(textureName, count));

		for(int i = 0; i < count; i++) {
			frames.add(TextureList.findTexture(textureName + "$" + i));
		}
	}
	
	private static BufferedImage toCombinedImage(String imageLoc, int count) {
	    if(count <= 0) {
	        return null;
	    }
	    
	    Image[] images = new BufferedImage[count];
	    
        for(int i = 0; i < count; i++) {
            images[i] = toImage(imageLoc + "$" + i);
        }
        
        int wid = images[0].getWidth(null) * count;
        int height = images[0].getHeight(null);
        //create a new buffer and draw two image into the new image
        BufferedImage newImage = new BufferedImage(wid,height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        //fill background
        g2.setPaint(new Color(0.0f, 0.0f, 0.0f, 0.0f));
        g2.fillRect(0, 0, wid, height);
        //draw image
        g2.setColor(oldColor);
        
        for(int i = 0; i < count; i++) {
            g2.drawImage(images[i], i * images[0].getWidth(null), 0, null);
        }
        g2.dispose();
        
        return newImage;
	}
	
	public Texture getCurrent() {
		return frames.get(getCurrentFrame());
	}
	
	@Override
	public int getCurrentFrame() {
	    return idx % count();
	}
	
	@Override
	public int count() {
	    return frames.size();
	}
}
