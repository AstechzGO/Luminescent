package astechzgo.luminescent.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.TextureList;

public class DisplayUtils {

	public static final int SCREEN_WIDTH = Display.getDisplayMode().getWidth();
	public static final int SCREEN_HEIGHT = Display.getDisplayMode().getHeight();
	
	public static final int WIDTH_OFFSET = Math.max(0, (SCREEN_WIDTH - (SCREEN_HEIGHT / 9 * 16)) / 2);
	public static final int HEIGHT_OFFSET = Math.max(0, (SCREEN_HEIGHT - (SCREEN_WIDTH / 16 * 9)) / 2);
	
	/**
	 * Set the display mode to be used
	 * 
	 * @param width
	 *            The width of the display required
	 * @param height
	 *            The height of the display required
	 * @param fullscreen
	 *            True if we want fullscreen mode
	 */
	public static void setDisplayMode(int width, int height, boolean fullscreen) {

		// return if requested DisplayMode is already set
		if ((Display.getDisplayMode().getWidth() == width)
				&& (Display.getDisplayMode().getHeight() == height)
				&& (Display.isFullscreen() == fullscreen)) {
			return;
		}

		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				DisplayMode[] modes = Display.getAvailableDisplayModes();
				int freq = 0;

				for (int i = 0; i < modes.length; i++) {
					DisplayMode current = modes[i];

					if ((current.getWidth() == width)
							&& (current.getHeight() == height)) {
						if ((targetDisplayMode == null)
								|| (current.getFrequency() >= freq)) {
							if ((targetDisplayMode == null)
									|| (current.getBitsPerPixel() > targetDisplayMode
											.getBitsPerPixel())) {
								targetDisplayMode = current;
								freq = targetDisplayMode.getFrequency();
							}
						}

						// if we've found a match for bpp and frequence against
						// the
						// original display mode then it's probably best to go
						// for this one
						// since it's most likely compatible with the monitor
						if ((current.getBitsPerPixel() == Display
								.getDesktopDisplayMode().getBitsPerPixel())
								&& (current.getFrequency() == Display
										.getDesktopDisplayMode().getFrequency())) {
							targetDisplayMode = current;
							break;
						}
					}
				}
			} else {
				targetDisplayMode = new DisplayMode(width, height);
			}

			if (targetDisplayMode == null) {
				System.out.println("Failed to find value mode: " + width + "x"
						+ height + " fs=" + fullscreen);
				return;
			}

			Display.setDisplayMode(targetDisplayMode);
			Display.setFullscreen(fullscreen);
			
		} catch (Exception e) {
			System.out.println("Unable to setup mode " + width + "x" + height
					+ " fullscreen=" + fullscreen + e);
		}
	}

	/**
	 * Sets the icons for display
	 * 
	 * @param nIcon
	 *            The locations of the icons
	 * @param c
	 *            A random object
	 */
	public static void setIcons(String[] nIcon, Object c) {
		List<ByteBuffer> icons = new ArrayList<ByteBuffer>();
		for (String name : nIcon) {
			icons.add(TextureList.findTexture(name).getAsByteBuffer());
		}

		Display.setIcon(icons.toArray(new ByteBuffer[icons.size()]));
	}

	public static void takeScreenshot(File file) throws Exception {
		if(!Display.isFullscreen()) {
			throw new Exception("Must be fullscreen to take screenshot");
		}
		
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = Display.getDisplayMode().getWidth();
		int height= Display.getDisplayMode().getHeight();
		int bpp = Display.getDisplayMode().getBitsPerPixel() / 8;
		ByteBuffer buffer = BufferUtils.createByteBuffer((width - DisplayUtils.WIDTH_OFFSET) * (height - DisplayUtils.HEIGHT_OFFSET) * bpp);
		GL11.glReadPixels(DisplayUtils.WIDTH_OFFSET, DisplayUtils.HEIGHT_OFFSET, width - DisplayUtils.WIDTH_OFFSET, height - DisplayUtils.HEIGHT_OFFSET, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		String format = "png";
		BufferedImage image = new BufferedImage(width - DisplayUtils.WIDTH_OFFSET * 2, height - DisplayUtils.HEIGHT_OFFSET * 2, BufferedImage.TYPE_INT_RGB);
		
		for(int x = 0; x < width - DisplayUtils.WIDTH_OFFSET * 2; x++) 
		{
		    for(int y = 0; y < height - DisplayUtils.HEIGHT_OFFSET * 2; y++)
		    {
		        int i = (x + ((width - DisplayUtils.WIDTH_OFFSET) * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(width - DisplayUtils.WIDTH_OFFSET * 2 - (x + 1), height - DisplayUtils.HEIGHT_OFFSET * 2 - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		   
		try {
		    ImageIO.write(image, format, file);
		} catch (IOException e) { LoggingUtils.logException(LoggingUtils.LOGGER, e); }
	}
}
