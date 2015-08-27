package astechzgo.luminescent.utils;

import static org.lwjgl.glfw.Callbacks.glfwSetCallback;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_DEBUG_CONTEXT;
import static org.lwjgl.glfw.GLFW.GLFW_RESIZABLE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetWindowPos;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import astechzgo.luminescent.textures.TextureList;

public class DisplayUtils {	
	
	private static long handle;
	
	private static GLContext context;
	
	private static boolean displayResizable = false;
	private static boolean displayFullscreen = false;
	
	private static DisplayMode mode = new DisplayMode(848, 477);
	private static DisplayMode desktopDisplayMode = new DisplayMode(848, 477);
	
	private static int displayWidth = 0;
	private static int displayHeight = 0;
	private static int displayFramebufferWidth = 0;
	private static int displayFramebufferHeight = 0;
	
	public static long monitor;
	public static ByteBuffer vidmode;

	public static int monitorWidth;
	public static int monitorHeight;

	public static int monitorBitPerPixel;
	public static int monitorRefreshRate;

	public static int widthOffset;
	public static int heightOffset;
	
	public static int oldGameWidth = getDisplayWidth() - widthOffset * 2;
	public static int oldGameHeight = getDisplayHeight() - heightOffset * 2;

	static {
		if ( glfwInit() != GL11.GL_TRUE )
			throw new IllegalStateException("Unable to initialize glfw");
		
		monitor = glfwGetPrimaryMonitor();
		vidmode = glfwGetVideoMode(monitor);
	
		monitorWidth = GLFWvidmode.width(vidmode);
		monitorHeight = GLFWvidmode.height(vidmode);
	
		monitorBitPerPixel = GLFWvidmode.redBits(vidmode) + GLFWvidmode.greenBits(vidmode) + GLFWvidmode.blueBits(vidmode);
		monitorRefreshRate = GLFWvidmode.refreshRate(vidmode);
	
		widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
		if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);
	}
	public static String displayTitle = "";
	
	
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
		if ((displayWidth == width)
				&& (displayHeight == height)
				&& (displayFullscreen == fullscreen)) {
			return;
		}

		try {
			DisplayMode targetDisplayMode = null;

			if (fullscreen) {
				IntBuffer count = BufferUtils.createIntBuffer(1);
				ByteBuffer modes = GLFW.glfwGetVideoModes(monitor, count);

				DisplayMode[] displayModes = new DisplayMode[count.get(0)];

				for (int i = 0; i < count.get(0); i++) {
					modes.position(i * GLFWvidmode.SIZEOF);

					int w = GLFWvidmode.width(modes);
					int h = GLFWvidmode.height(modes);
					int b = GLFWvidmode.redBits(modes) + GLFWvidmode.greenBits(modes)
							+ GLFWvidmode.blueBits(modes);
					int r = GLFWvidmode.refreshRate(modes);
					
					displayModes[i] = new DisplayMode(w, h, b, r);
				}
				
				int freq = 0;

				for (int i = 0; i < displayModes.length; i++) {
					DisplayMode current = displayModes[i];

					if ((current.WIDTH == width)
							&& (current.HEIGHT == height)) {
						if ((targetDisplayMode == null)
								|| (current.FREQ >= freq)) {
							if ((targetDisplayMode == null)
									|| (current.BPP > targetDisplayMode
											.BPP)) {
								targetDisplayMode = current;
								freq = targetDisplayMode.FREQ;
							}
						}

						// if we've found a match for bpp and frequence against
						// the
						// original display mode then it's probably best to go
						// for this one
						// since it's most likely compatible with the monitor
						if ((current.BPP == desktopDisplayMode.BPP)
								&& (current.FREQ == desktopDisplayMode.FREQ)) {
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

			if (fullscreen == displayFullscreen)
		        return;

			mode = targetDisplayMode;
			
		 long newWindow;
	        displayFullscreen = fullscreen;
	        if(displayFullscreen){
	            newWindow = glfwCreateWindow(mode.WIDTH, mode.HEIGHT, displayTitle, monitor, handle);
	            glfwMakeContextCurrent( newWindow );	            
	        }else{
	            newWindow = glfwCreateWindow(mode.WIDTH, mode.HEIGHT, displayTitle, NULL, handle);
	            
	            int monitorWidthOffset = getMonitorOffsetWidth(DisplayUtils.monitor);
	    		int monitorHeightOffset = getMonitorOffsetHeight(DisplayUtils.monitor);
	            
	    		glfwSetWindowPos(
	    				newWindow,
	    				((monitorWidth - mode.WIDTH) / 2) + monitorWidthOffset,
	    				((monitorHeight - mode.HEIGHT) / 2) + monitorHeightOffset
	    			);
	    		
	            glfwMakeContextCurrent( newWindow );
	        }
	        glfwDestroyWindow(handle);
	        handle = newWindow;

	        displayWidth = mode.WIDTH;
	        displayHeight = mode.HEIGHT;
	        
	        widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
			if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);
	        
	        glfwSwapInterval(1);
	        glfwShowWindow(handle);
	        GLContext.createFromCurrent();

	        GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, mode.WIDTH, 0, mode.HEIGHT, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			
			if(fullscreen)
				GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

			glfwSetCallback(handle, KeyboardUtils.KEY_CALLBACK);
			
			
		} catch (Exception e) {
			System.out.println("Unable to setup mode " + width + "x" + height
					+ " fullscreen=" + fullscreen + e);
			e.printStackTrace();
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

		//Display.setIcon(icons.toArray(new ByteBuffer[icons.size()]));
	}

	public static void takeScreenshot(File file) throws Exception {
		GL11.glReadBuffer(GL11.GL_FRONT);
		int width = displayWidth;
		int height= displayHeight;
		int bpp = (monitorBitPerPixel / 8) + 1;  //For alpha
		ByteBuffer buffer = BufferUtils.createByteBuffer((width - DisplayUtils.widthOffset) * (height - DisplayUtils.heightOffset) * bpp);
		GL11.glReadPixels(DisplayUtils.widthOffset, DisplayUtils.heightOffset, width - DisplayUtils.widthOffset, height - DisplayUtils.heightOffset, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
		String format = "png";
		BufferedImage image = new BufferedImage(width - DisplayUtils.widthOffset * 2, height - DisplayUtils.heightOffset * 2, BufferedImage.TYPE_INT_RGB);
		
		for(int x = 0; x < width - DisplayUtils.widthOffset * 2; x++) 
		{
		    for(int y = 0; y < height - DisplayUtils.heightOffset * 2; y++)
		    {
		        int i = (x + ((width - DisplayUtils.widthOffset) * y)) * bpp;
		        int r = buffer.get(i) & 0xFF;
		        int g = buffer.get(i + 1) & 0xFF;
		        int b = buffer.get(i + 2) & 0xFF;
		        image.setRGB(width - DisplayUtils.widthOffset * 2 - (x + 1), height - DisplayUtils.heightOffset * 2 - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
		    }
		}
		   
		image = getFlippedImage(image);
		
		try {
		    ImageIO.write(image, format, file);
		} catch (IOException e) { LoggingUtils.logException(LoggingUtils.LOGGER, e); }
	}
	
    public static BufferedImage getFlippedImage(BufferedImage bi) {
        BufferedImage flipped = new BufferedImage(
                bi.getWidth(),
                bi.getHeight(),
                bi.getType());
        AffineTransform tran = AffineTransform.getTranslateInstance(bi.getWidth(), 0);
        AffineTransform flip = AffineTransform.getScaleInstance(-1d, 1d);
        tran.concatenate(flip);

        Graphics2D g = flipped.createGraphics();
        g.setTransform(tran);
        g.drawImage(bi, 0, 0, null);
        g.dispose();

        return flipped;
    }
	
	private static class DisplayMode {
		private final int WIDTH, HEIGHT, BPP, FREQ;
		public DisplayMode(int width, int height, int bpp, int freq) {
			this(width, height, bpp, freq, true);
		}
		
		public DisplayMode(int width, int height) {
			this(width, height, 0, 0, false);
		}
		
		private DisplayMode(int width, int height, int bpp, int freq, boolean fullscreen) {
			this.WIDTH = width;
			this.HEIGHT = height;
			this.BPP = bpp;
			this.FREQ = freq;
		}
	}
	
	public static void create() {
		long monitor = glfwGetPrimaryMonitor();
		ByteBuffer vidmode = glfwGetVideoMode(monitor);

		int monitorWidth = GLFWvidmode.width(vidmode);
		int monitorHeight = GLFWvidmode.height(vidmode);
		int monitorBitPerPixel = GLFWvidmode.redBits(vidmode) + GLFWvidmode.greenBits(vidmode) + GLFWvidmode.blueBits(vidmode);
		int monitorRefreshRate = GLFWvidmode.refreshRate(vidmode);
		
		desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, displayResizable ? GL_TRUE : GL_FALSE);
		glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GL_TRUE);

		
		handle = glfwCreateWindow(mode.WIDTH, mode.HEIGHT,  displayTitle, NULL, NULL);
		if ( handle == 0L )
			throw new IllegalStateException("Failed to create Display window");
		
		displayWidth = mode.WIDTH;
		displayHeight = mode.HEIGHT;
		
		IntBuffer fbw = BufferUtils.createIntBuffer(1);	
		IntBuffer fbh = BufferUtils.createIntBuffer(1);
		GLFW.glfwGetFramebufferSize(handle, fbw, fbh);
		displayFramebufferWidth = fbw.get(0);
		displayFramebufferHeight = fbh.get(0);
		
		int monitorWidthOffset = getMonitorOffsetWidth(DisplayUtils.monitor);
		int monitorHeightOffset = getMonitorOffsetHeight(DisplayUtils.monitor);
		
		glfwSetWindowPos(
			handle,
			monitorWidthOffset + (DisplayUtils.monitorWidth / 2) - (displayWidth / 2),
			monitorHeightOffset + (DisplayUtils.monitorHeight / 2) - (displayHeight / 2)
		);
		
		glfwMakeContextCurrent(handle);
		context = GLContext.createFromCurrent();
		
		glfwSwapInterval(1);
		glfwShowWindow(handle);
	}
	
	public static long getHandle() {
		return handle;
	}
	
	public static boolean isFullscreen() {
		return displayFullscreen;
	}
	
	public static int getDisplayFramebufferWidth() {
		return displayFramebufferWidth;
	}
	
	public static int getDisplayFramebufferHeight() {
		return displayFramebufferHeight;
	}	
	
	public static GLContext getContext() {
		return context;
	}
	
	public static int getDisplayWidth() {
		return displayWidth;
	}
	
	public static int getDisplayHeight() {
		return displayHeight;
	}
	
	public static void changeMonitor(long monitor) {
		DisplayUtils.monitor = monitor;
		vidmode = glfwGetVideoMode(monitor);
	
		monitorWidth = GLFWvidmode.width(vidmode);
		monitorHeight = GLFWvidmode.height(vidmode);
	
		monitorBitPerPixel = GLFWvidmode.redBits(vidmode) + GLFWvidmode.greenBits(vidmode) + GLFWvidmode.blueBits(vidmode);
		monitorRefreshRate = GLFWvidmode.refreshRate(vidmode);
	
		widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
		if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);
	}
	
	public static long getWindow() {
		return handle;
	}
	
	public static int getMonitorOffsetWidth(long monitor) {
		IntBuffer xpos = BufferUtils.createIntBuffer(1);
		IntBuffer ypos = BufferUtils.createIntBuffer(1);
		
		GLFW.glfwGetMonitorPos(monitor, xpos, ypos);
		
		int monitorOffsetWidth = xpos.get();
		
		xpos.clear();
		ypos.clear();
		
		return monitorOffsetWidth;
	}
	
	public static int getMonitorOffsetHeight(long monitor) {
		IntBuffer xpos = BufferUtils.createIntBuffer(1);
		IntBuffer ypos = BufferUtils.createIntBuffer(1);
		
		GLFW.glfwGetMonitorPos(monitor, xpos, ypos);
		
		int monitorOffsetHeight = ypos.get();
		
		xpos.clear();
		ypos.clear();
		
		return monitorOffsetHeight;
	}
	
	public static void nextMonitor() {
		//Pointer to array
		long[] monitors = new long[GLFW.glfwGetMonitors().capacity()];
		int q = 0;
		while(q < GLFW.glfwGetMonitors().capacity()) {
			monitors[q] = GLFW.glfwGetMonitors().get(q);
			q++;
		}		
		
		
		int nextMonitorIdx = 0;
		int currentMonitorIdx = 0;
		
		int monitorOffsetWidth = 0;
		int monitorOffsetHeight = 0;
		
		int secondMonitorWidth = 0;
		int secondMonitorHeight = 0;
		
		if(DisplayUtils.isFullscreen()) {
			for(int i = 0; i < monitors.length; i++) {
				if(monitors[i] == GLFW.glfwGetWindowMonitor(DisplayUtils.getWindow())) {
					currentMonitorIdx = i;
				}
			}
				
			if(currentMonitorIdx != (monitors.length - 1))
				nextMonitorIdx = currentMonitorIdx + 1;
			else
				nextMonitorIdx = 0;
		
		}
		else {
			IntBuffer xpos = BufferUtils.createIntBuffer(1);
			IntBuffer ypos = BufferUtils.createIntBuffer(1);

			for(int i = 0; i < monitors.length; i++) {
				
				monitorOffsetWidth = getMonitorOffsetWidth(monitors[i]);
				monitorOffsetHeight = getMonitorOffsetHeight(monitors[i]);

				ByteBuffer mode = GLFW.glfwGetVideoMode(monitors[i]);
				
				secondMonitorWidth = GLFWvidmode.width(mode);
				
				secondMonitorHeight = GLFWvidmode.height(mode);
				
				Rectangle r = new Rectangle(monitorOffsetWidth, monitorOffsetHeight, secondMonitorWidth, secondMonitorHeight);
				
				GLFW.glfwGetWindowPos(DisplayUtils.getWindow(), xpos, ypos);
				
				int x = xpos.get();
				int y = ypos.get();
				
				xpos.clear();
				ypos.clear();
				
				if(r.contains(x, y)) {
					currentMonitorIdx = i;
					if(currentMonitorIdx != (monitors.length - 1))
						nextMonitorIdx = currentMonitorIdx + 1;
					else
						nextMonitorIdx = 0;
					break;
				}
			}
			
		}
		
		changeMonitor(monitors[nextMonitorIdx]);
		

		if(DisplayUtils.isFullscreen()) {
			setDisplayMode(GLFWvidmode.width(DisplayUtils.vidmode),
					GLFWvidmode.height(DisplayUtils.vidmode), false);
			
			setDisplayMode(GLFWvidmode.width(DisplayUtils.vidmode),
					GLFWvidmode.height(DisplayUtils.vidmode), true);
		}
		else {
			IntBuffer xpos = BufferUtils.createIntBuffer(1);
			IntBuffer ypos = BufferUtils.createIntBuffer(1);
			
			GLFW.glfwGetMonitorPos(monitors[nextMonitorIdx], xpos, ypos);
			
			monitorOffsetWidth = xpos.get();
			monitorOffsetHeight = ypos.get();
			
			xpos.clear();
			ypos.clear();
			
			GLFW.glfwSetWindowPos(
					DisplayUtils.getWindow(), 
					monitorOffsetWidth + (GLFWvidmode.width(vidmode) / 2) - (getDisplayWidth() / 2),
					monitorOffsetHeight + (GLFWvidmode.height(vidmode) / 2) - (getDisplayHeight() / 2)
				);
		}
		
		KeyboardUtils.resetKeys();
	}
}
