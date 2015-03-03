package astechzgo.luminescent.utils;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import astechzgo.luminescent.textures.Texture;

public class RenderingUtils
{
	public static void RenderQuad(int x, int y, int width, int height)
	{
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0,0);
		GL11.glVertex2d(x, y);
		GL11.glTexCoord2f(1,0);
		GL11.glVertex2d(x + width, y);
		GL11.glTexCoord2f(1,1);
		GL11.glVertex2d(x + width, y + height);
		GL11.glTexCoord2f(0, 1);
		GL11.glVertex2d(x, y + height);
		
		GL11.glEnd();
	}
	
	/**
	 * Messes up some textures
	 */
	public static void RenderQuad(int x, int y, Texture texture)
	{
		if(texture.getAsSlickTexture() != null) 
		{
			Color.white.bind();
			texture.getAsSlickTexture().bind();	
			RenderQuad(x, y, texture.getAsSlickTexture().getImageWidth(), texture.getAsSlickTexture().getImageHeight());
		}
	}
}