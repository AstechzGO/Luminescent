package astechzgo.luminescent.textures;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.system.MemoryUtil;

public class Texture {
	
	private final BufferedImage asBufferedImage;
	private final ByteBuffer asByteBuffer;
	
	private final String name;
	
	public Texture(String textureName, boolean slick) {
		this(textureName, slick, toImage(textureName));
	}
	
	public Texture(String textureName, boolean slick, Image asImage) {
		name = textureName;
		
		if(!slick) {
			asBufferedImage = toBufferedImage(asImage);
			asByteBuffer = toByteBuffer(asBufferedImage);
		}
		else {
			asBufferedImage = toBufferedImage(asImage);
			asByteBuffer = toByteBuffer(asBufferedImage);
		}
	}
	
	/**
	 * Convert BufferedImage to ByteBuffer
	 * 
	 * @param image
	 *            The BufferedImage to convert
	 * @return The converted image
	 */
	private ByteBuffer toByteBuffer(BufferedImage image) {
		int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        
        ByteBuffer buffer = MemoryUtil.memAlloc(image.getWidth() * image.getHeight() * 4); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < image.getHeight(); y++){
            for(int x = 0; x < image.getWidth(); x++){
                int pixel = pixels[y * image.getWidth() + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      // Green component
                buffer.put((byte) (pixel & 0xFF));               // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    // Alpha component. Only for RGBA
            }
        }

        buffer.flip(); //FOR THE LOVE OF GOD DO NOT FORGET THIS
        
        return buffer;
	}
	
	protected static Image toImage(String imageLoc) {
		imageLoc = imageLoc.replaceAll("\\.", "/");
        try {
            return ImageIO.read(getResourceAsURL("textures/" + imageLoc + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

		return null;
	}
	
	public BufferedImage getAsBufferedImage() {
		return asBufferedImage;
	}
	
	public ByteBuffer getAsByteBuffer() {
		return asByteBuffer;
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
    private BufferedImage toBufferedImage(Image img) {
		if (img instanceof BufferedImage) {
		     // Return the buffered image
	         return (BufferedImage) img;
		}
		
		// Create a buffered image with transparency
		BufferedImage bimage = new BufferedImage(img.getWidth(null),
				img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

		// Draw the image on to the buffered image
		Graphics2D bGr = bimage.createGraphics();
		bGr.drawImage(img, 0, 0, null);
		bGr.dispose();
		
		// Return the buffered image
		return bimage;
	}
    
	public String getName() {
		return name;
	}
    
    void dispose() {
    	MemoryUtil.memFree(asByteBuffer);
    }
    
    public int getCurrentFrame() {
        return 0;
    }
    
    public int count() {
        return 1;
    }
}