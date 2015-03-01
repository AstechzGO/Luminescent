package astechzgo.luminescent.utils;

import org.lwjgl.opengl.GL11;

public class RenderingUtils
{
	public static void RenderQuad(int x, int y, int width, int height)
	{
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glVertex2d(x, y);
		GL11.glVertex2d(x + width, y);
		GL11.glVertex2d(x + width, y + height);
		GL11.glVertex2d(x, y + height);
		
		GL11.glEnd();
	}
	
	public static void RenderTexturedQuad(int x, int y, int width, int height)
	{
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, somethingelse);
		RenderQuad(x, y, width, height);
	}
}