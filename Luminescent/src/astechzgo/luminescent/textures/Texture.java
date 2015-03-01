package astechzgo.luminescent.textures;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {
	
	private final BufferedImage asBufferedImage;
	private final ByteBuffer asByteBuffer;
	
	public Texture(String textureName) {
		asBufferedImage = toBufferedImage(textureName);
		asByteBuffer = toByteBuffer(asBufferedImage);
	}
	
	public Texture(String textureName, String dirName) {
		asBufferedImage = toBufferedImage(textureName, dirName);
		asByteBuffer = toByteBuffer(asBufferedImage);
	}
	
	/**
	 * Convert BufferedImage to ByteBuffer
	 * 
	 * @param image
	 *            The BufferedImage to convert
	 * @return The converted image
	 */
	private ByteBuffer toByteBuffer(BufferedImage image) {
		ByteBuffer buf = null;
		
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			ImageIO.write(image,"png", os); 
			InputStream fis = new ByteArrayInputStream(os.toByteArray());
			// Link the PNG decoder to this stream
		    PNGDecoder decoder = new PNGDecoder(fis);
		      
		    // Decode the PNG file in a ByteBuffer
		    buf = ByteBuffer.allocateDirect(
		            4 * decoder.getWidth() * decoder.getHeight());
		    decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
		    buf.flip();
		     
		    fis.close();
		    
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return buf;
	}
	
	private BufferedImage toBufferedImage(String imageLoc) {
		Image img = new ImageIcon(this.getClass().getResource(
				"/resources/textures/" + imageLoc + ".png")).getImage();
		return toBufferedImage(img);
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img
	 *            The Image to be converted
	 * @return The converted BufferedImage
	 */
	private BufferedImage toBufferedImage(String imageLoc, String dirName) {
		Image img = new ImageIcon(this.getClass().getResource(
				"/resources/textures/"+ dirName + "/" + imageLoc + ".png")).getImage();
		return toBufferedImage(img);
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
		return bimage;
	}
}
