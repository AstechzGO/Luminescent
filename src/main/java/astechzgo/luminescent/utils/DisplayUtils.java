package astechzgo.luminescent.utils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import astechzgo.luminescent.main.Luminescent;
import org.lwjgl.glfw.*;
import org.lwjgl.glfw.GLFWVidMode.Buffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.textures.TextureList;

public class DisplayUtils {	
	
	private static long handle;

	private static boolean displayFullscreen = false;
	
	private static DisplayMode mode = new DisplayMode(848, 477);
	private static DisplayMode desktopDisplayMode = new DisplayMode(848, 477);
	
	private static int displayWidth = 0;
	private static int displayHeight = 0;
	private static int displayFramebufferWidth = 0;
	private static int displayFramebufferHeight = 0;
	
	public static long monitor;
	public static GLFWVidMode vidmode;

	public static int monitorWidth;
	public static int monitorHeight;

	public static int monitorBitPerPixel;
	public static int monitorRefreshRate;

	public static int widthOffset;
	public static int heightOffset;
	
	public static int oldGameWidth = getDisplayWidth() - widthOffset * 2;
	public static int oldGameHeight = getDisplayHeight() - heightOffset * 2;
	
	public static int displayX;
	public static int displayY;
	
	private static GLFWImage.Buffer icons;

	static {
		if (!glfwInit())
			throw new IllegalStateException("Unable to initialize glfw");
		
		monitor = glfwGetPrimaryMonitor();
		vidmode = glfwGetVideoMode(monitor);
	
		monitorWidth = vidmode.width();
		monitorHeight = vidmode.height();
	
		monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
		monitorRefreshRate = vidmode.refreshRate();
	
		widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
		if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);
	}
	public static String displayTitle = "Luminescent";
	
	
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
		DisplayUtils.displayFullscreen = fullscreen;
		displayWidth = width;
		displayHeight = height;
		displayX = (DisplayUtils.monitorWidth / 2) - (displayWidth / 2);
		displayY = (DisplayUtils.monitorHeight / 2) - (displayHeight / 2);

		GLFW.glfwSetWindowMonitor(handle, fullscreen ? monitor : NULL, displayX, displayY, width, height, monitorRefreshRate);
	}

	/**
	 * Sets the icons for display
	 * 
	 * @param nIcon The locations of the icons
	 */
	public static void setIcons(String[] nIcon) {
	    if(nIcon == null) {
	        if(icons != null) {
	            icons.free();
	        }
	        return;
	    }
	    
		GLFWImage.Buffer icons = GLFWImage.calloc(nIcon.length);
		
		int i = 0;
		for (String name : nIcon) {
			ByteBuffer buffer = TextureList.findTexture(name).getAsByteBuffer();
			int width = TextureList.findTexture(name).getAsBufferedImage().getWidth();
			int height = TextureList.findTexture(name).getAsBufferedImage().getHeight();
			
			icons.position(i++).width(width).height(height).pixels(buffer);
		}
		
		DisplayUtils.icons = icons;
	}

	public static void takeScreenshot(File file) {
		//GL11.glReadBuffer(GL11.GL_FRONT);
		int width = displayWidth;
		int height= displayHeight;
		int bpp = (monitorBitPerPixel / 8) + 1;  //For alpha
		
		ByteBuffer buffer = MemoryUtil.memAlloc((width - DisplayUtils.widthOffset) * (height - DisplayUtils.heightOffset) * bpp);
		buffer.put(Vulkan.readPixels(DisplayUtils.widthOffset, DisplayUtils.heightOffset, width - DisplayUtils.widthOffset, height - DisplayUtils.heightOffset));
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
		   
		MemoryUtil.memFree(buffer);
		
		image = getFlippedImage(image);
		
		try {
		    ImageIO.write(image, format, file);
		} 
		catch (IOException e) { 
			e.printStackTrace();
		}
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

        return getFlipped2Image(flipped);
    }
	
    private static BufferedImage getFlipped2Image(BufferedImage bi) {
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
		GLFWVidMode vidmode = glfwGetVideoMode(monitor);

		int monitorWidth = vidmode.width();
		int monitorHeight = vidmode.height();
		int monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.redBits();
		int monitorRefreshRate = vidmode.refreshRate();
		
		desktopDisplayMode = new DisplayMode(monitorWidth, monitorHeight, monitorBitPerPixel, monitorRefreshRate);

		glfwDefaultWindowHints();
		glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API);
		glfwWindowHint(GLFW_VISIBLE, GLFW.GLFW_FALSE);
		boolean displayResizable = true;
		glfwWindowHint(GLFW_RESIZABLE, displayResizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
		
		handle = glfwCreateWindow(mode.WIDTH, mode.HEIGHT,  displayTitle, NULL, NULL);
		if ( handle == 0L )
			throw new IllegalStateException("Failed to create Display window");
		
		displayWidth = mode.WIDTH;
		displayHeight = mode.HEIGHT;
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer fbw = stack.mallocInt(1);
			IntBuffer fbh = stack.mallocInt(1);
			GLFW.glfwGetFramebufferSize(handle, fbw, fbh);
			displayFramebufferWidth = fbw.get(0);
			displayFramebufferHeight = fbh.get(0);
		}
		
		int monitorWidthOffset = getMonitorOffsetWidth(DisplayUtils.monitor);
		int monitorHeightOffset = getMonitorOffsetHeight(DisplayUtils.monitor);
		
		displayX = monitorWidthOffset + (DisplayUtils.monitorWidth / 2) - (displayWidth / 2);
		displayY = monitorHeightOffset + (DisplayUtils.monitorHeight / 2) - (displayHeight / 2);
		
		glfwSetWindowPos(
			handle,
			displayX,
			displayY
		);
		
		glfwMakeContextCurrent(handle);
		
		GLFW.glfwSetWindowIcon(handle, icons);
		
		GLFW.glfwSetWindowSizeCallback(handle, RESIZED_CALLBACK);
		GLFW.glfwSetWindowPosCallback(handle, REPOSITIONED_CALLBACK);

		if(Luminescent.DEBUG) {
			GLFW.glfwSetErrorCallback(ERROR_CALLBACK);
		}


		GLFW.glfwSetWindowAspectRatio(handle, 16, 9);

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
	
	public static int getDisplayWidth() {
		return displayWidth;
	}
	
	public static int getDisplayHeight() {
		return displayHeight;
	}
	
	public static void changeMonitor(long monitor) {
		DisplayUtils.monitor = monitor;
		vidmode = glfwGetVideoMode(monitor);
	
		monitorWidth = vidmode.width();
		monitorHeight = vidmode.height();
	
		monitorBitPerPixel = vidmode.redBits() + vidmode.greenBits() + vidmode.blueBits();
		monitorRefreshRate = vidmode.refreshRate();
	
		widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
		if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);
	}
	
	public static long getWindow() {
		return handle;
	}
	
	public static int getMonitorOffsetWidth(long monitor) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer xpos = stack.mallocInt(1);
			IntBuffer ypos = stack.mallocInt(1);
	
			GLFW.glfwGetMonitorPos(monitor, xpos, ypos);
		
			return xpos.get();
		}
	}
	
	public static int getMonitorOffsetHeight(long monitor) {
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer xpos = stack.mallocInt(1);
			IntBuffer ypos = stack.mallocInt(1);
	
			GLFW.glfwGetMonitorPos(monitor, xpos, ypos);
		
			return ypos.get();
		}
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

		for(int i = 0; i < monitors.length; i++) {
				
			monitorOffsetWidth = getMonitorOffsetWidth(monitors[i]);
			monitorOffsetHeight = getMonitorOffsetHeight(monitors[i]);

			GLFWVidMode mode = GLFW.glfwGetVideoMode(monitors[i]);
				
			secondMonitorWidth = mode.width();
				
			secondMonitorHeight = mode.height();
				
			Rectangle r = new Rectangle(monitorOffsetWidth, monitorOffsetHeight, secondMonitorWidth, secondMonitorHeight);
				
			if(r.contains(displayX, displayY)) {
				currentMonitorIdx = i;
				if(currentMonitorIdx != (monitors.length - 1))
					nextMonitorIdx = currentMonitorIdx + 1;
				else
					nextMonitorIdx = 0;
				break;
			}
		}
		
		changeMonitor(monitors[nextMonitorIdx]);
		

		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer xpos = stack.mallocInt(1);
			IntBuffer ypos = stack.mallocInt(1);
			
			GLFW.glfwGetMonitorPos(monitors[nextMonitorIdx], xpos, ypos);
			
			monitorOffsetWidth = xpos.get();
			monitorOffsetHeight = ypos.get();
		}
		
		displayX = monitorOffsetWidth + (DisplayUtils.vidmode.width() / 2) - (getDisplayWidth() / 2);
		displayY = monitorOffsetHeight + (DisplayUtils.vidmode.height() / 2) - (getDisplayHeight() / 2);
		
		GLFW.glfwSetWindowPos(
				DisplayUtils.getWindow(), 
				displayX,
				displayY
			);
		
		
		KeyboardUtils.resetKeys();
	}
	
	public static final GLFWWindowSizeCallback RESIZED_CALLBACK = new GLFWWindowSizeCallback() {

		@Override
		public void invoke(long window, int width, int height) {
			DisplayUtils.changeSize();
		}
		
	};
	
	public static final GLFWWindowPosCallback REPOSITIONED_CALLBACK = new GLFWWindowPosCallback() {

		@Override
		public void invoke(long window, int xpos, int ypos) {
			displayX = xpos;
			displayY = ypos;
		}
		
	};

	public static final GLFWErrorCallback ERROR_CALLBACK = new GLFWErrorCallback() {

		@Override
		public void invoke(int error, long description) {
			System.out.println("GLFW error code " + error + " - " + MemoryUtil.memUTF8(description));
		}

	};
	
	public static void changeSize() {
		int width = 0;
		int height = 0;
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			GLFW.glfwGetWindowSize(handle, w, h);
			width = w.get(0);
			height = h.get(0);
		}
		
		// return if requested DisplayMode is already set
		if ((displayWidth == width)
				&& (displayHeight == height)) {
			return;
		}

		try {

			mode = new DisplayMode(width, height);

	        displayWidth = mode.WIDTH;
	        displayHeight = mode.HEIGHT;
	        
	        widthOffset = Math.max(0, (displayWidth - (displayHeight / 9 * 16)) / 2);
			if(widthOffset == 0) heightOffset = Math.max(0, (displayHeight - (displayWidth / 16 * 9)) / 2);

			Vulkan.recreate();
			
			
		} catch (Exception e) {
			System.out.println("Unable to setup mode " + width + "x" + height + e);
			e.printStackTrace();
		}
	}
	
	public static int getDisplayX() {
		return displayX;
	}
	
	public static int getDisplayY() {
		return displayY;
	}

	public static void freeErrorCallback() {
		ERROR_CALLBACK.free();
	}
}
