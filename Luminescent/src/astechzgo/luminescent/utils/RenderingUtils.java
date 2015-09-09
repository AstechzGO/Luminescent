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
	    GL11.glBegin(GL11.GL_POLYGON);

	    for (double angle=0.0; angle<360.0; angle+=pointSeperation)
	    {
	    	double radian = Math.toRadians(angle);

	    	double xcos = (double)Math.cos(radian);
	    	double ysin = (float)Math.sin(radian);
	    	double tempx = xcos * radius + x;
	    	double tempy = ysin * radius + y;
	    	double tx = xcos * 0.5 + 0.5;
	    	double ty = ysin * 0.5 + 0.5;

	    	GL11.glTexCoord2d(tx, ty);
	    	GL11.glVertex2d(tempx, tempy);
	    }

	    GL11.glEnd();
	}
	
	public static void RenderCircle(double x, double y, double radius, double pointSeperation, Texture texture) 
	{
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    GL11.glColor3f(1, 1, 1);
	    
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture()); 
	    RenderCircle(x, y, radius, pointSeperation);
	    
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}