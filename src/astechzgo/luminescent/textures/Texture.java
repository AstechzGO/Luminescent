package astechzgo.luminescent.textures;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;

public class Texture {
	
	private final BufferedImage asBufferedImage;
	private final ByteBuffer asByteBuffer;
	private final int textureNumber;
	
	private final String name;
	
	public Texture(String textureName, boolean slick) {
		this(textureName, slick, toImage(textureName));
	}
	
	public Texture(String textureName, boolean slick, Image asImage) {
		name = textureName;
		
		if(!slick) {
			textureNumber = -1;
			asBufferedImage = toBufferedImage(asImage);
			asByteBuffer = toByteBuffer(asBufferedImage);
		}
		else {
			asBufferedImage = toBufferedImage(asImage);
			asByteBuffer = toByteBuffer(asBufferedImage);
			textureNumber = loadTexture();
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
	
	private static Image toImage(String imageLoc) {
		imageLoc = imageLoc.replaceAll("\\.", "/");
		Image img = new ImageIcon(Texture.class.getResource(
				"/resources/textures/" + imageLoc + ".png")).getImage();
		return img;
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
		if(textureNumber == -1)
			return bimage;
		else
			return getFlippedImage(bimage);
	}
	
	private int loadTexture() {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(asBufferedImage,"png", os); 
			
			int textureID = GL11.glGenTextures(); //Generate texture ID
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID); //Bind texture ID

            //Setup texture scaling filtering
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
			
            //Send texel data to OpenGL
			GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, asBufferedImage.getWidth(), asBufferedImage.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, asByteBuffer);

			
			
            //Return the texture ID so we can bind it later again
          return textureID;
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public int getAsTexture() {
		return textureNumber;
	}
	
	public String getName() {
		return name;
	}
	
    private BufferedImage getFlippedImage(BufferedImage bi) {
        BufferedImage flipped = new BufferedImage(
                bi.getWidth(),
                bi.getHeight(),
                bi.getType());
        AffineTransform tran = AffineTransform.getTranslateInstance(0, bi.getHeight());
        AffineTransform flip = AffineTransform.getScaleInstance(1d, -1d);
        tran.concatenate(flip);

        Graphics2D g = flipped.createGraphics();
        g.setTransform(tran);
        g.drawImage(bi, 0, 0, null);
        g.dispose();

        return flipped;
    }
    
    void dispose() {
    	MemoryUtil.memFree(asByteBuffer);
    }
}