package astechzgo.luminescent.utils;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.Texture;

public class RenderingUtils
{
	
	public static final double RADIAN = 0.01745329251994329576923690768489d;
	
	public static void RenderQuad(int x, int y, int width, int height)
	{
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0,1);
		GL11.glVertex2d(x, y);
		GL11.glTexCoord2f(1,1);
		GL11.glVertex2d(x + width, y);
		GL11.glTexCoord2f(1,0);
		GL11.glVertex2d(x + width, y + height);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2d(x, y + height);
		
		
		GL11.glEnd();
	}
	
	public static void RenderQuad(int x, int y, int width, int height, Texture texture)
	{
		if(texture.getAsTexture() != 0) 
		{
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL11.glColor3f(1, 1, 1);
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
			RenderQuad(x, y, width, height);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static void RenderCircle(double x, double y, double radius, double pointSeperation)
	{	
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		
		GL11.glVertex2d(x, y);
		
		for (int degrees = 0; degrees < 360 + pointSeperation; degrees += pointSeperation)
		{
			double degreeInRadians = degrees * RADIAN;
			GL11.glVertex2d(x + Math.sin(degreeInRadians) * radius, y + Math.cos(degreeInRadians) * radius);
		}
		
		GL11.glEnd();
	}
}