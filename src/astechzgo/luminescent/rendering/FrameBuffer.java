package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.utils.DisplayUtils;

public class FrameBuffer {

	private int width;
	private int height;
	
	private final int framebufferHandle;
	private final int colourTextureHandle;
	
	private static int screenHandle = 0;
	
	public FrameBuffer(int width, int height) {
		this.width = width;
		this.height = height;

        colourTextureHandle = GL11.glGenTextures();
		framebufferHandle = EXTFramebufferObject.glGenFramebuffersEXT();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTextureHandle);

	    EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferHandle);

	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, colourTextureHandle);
	    GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
	    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, this.width, this.height, 0,GL11.GL_RGBA, GL11.GL_INT, (java.nio.ByteBuffer) null);
	    EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT,GL11.GL_TEXTURE_2D, colourTextureHandle, 0);
 
	    EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, 0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		int result = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);

		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, screenHandle);

		if (result != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {

			if (result == EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete attachment");
			if (result == EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: incomplete dimensions");
			if (result == EXTFramebufferObject.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: missing attachment");
			if (result == EXTFramebufferObject.GL_FRAMEBUFFER_UNSUPPORTED_EXT)
				throw new IllegalStateException("Frame buffer couldn't be constructed: unsupported combination of formats");
			throw new IllegalStateException("Frame buffer couldn't be constructed: unknown error " + result);
		}
	}

	public void renderToScreen() {
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, screenHandle);
		GL11.glViewport(0, 0, DisplayUtils.getDisplayWidth(), DisplayUtils.getDisplayHeight());
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, DisplayUtils.getDisplayWidth(), 0, DisplayUtils.getDisplayHeight(), 1, -1);
	}

	public void renderToFBO() {
		EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, framebufferHandle);
		GL11.glViewport( 0, 0, width, height );
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, 0, height, 1, -1);
	}

	public int getColourTextureHandle() {
		return colourTextureHandle;
	}

	public int getFramebufferHandle() {
		return framebufferHandle;
	}
}
