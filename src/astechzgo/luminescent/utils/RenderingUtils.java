package astechzgo.luminescent.utils;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.Texture;

public class RenderingUtils
{
	
	/*		A--B
	 * 		|QD|
	 * 		D--C
	 */
	public static void RenderQuad(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY)
	{
		GL11.glBegin(GL11.GL_QUADS);
		
		GL11.glTexCoord2f(0,1);
		GL11.glVertex2d(aX, aY);
		GL11.glTexCoord2f(1,1);
		GL11.glVertex2d(bX, bY);
		GL11.glTexCoord2f(1,0);
		GL11.glVertex2d(cX, cY);
		GL11.glTexCoord2f(0, 0);
		GL11.glVertex2d(dX, dY);
		
		
		GL11.glEnd();
	}
	
	public static void RenderQuad(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY, Texture texture)
	{
		if(texture.getAsTexture() != 0) 
		{
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL11.glColor3f(1, 1, 1);
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
			RenderQuad(aX, aY, bX, bY, cX, cY, dX, dY);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}
	
	public static void RenderCircle(int x, int y, double radius, double pointSeperation, double rotation)
	{	
	    GL11.glBegin(GL11.GL_POLYGON);

	    for (double angle=0; angle<360.0; angle+=pointSeperation)
	    {
	    	double radian = Math.toRadians(angle);

	    	double xcos = (double)Math.cos(radian);
	    	double ysin = (float)Math.sin(radian);
	    	double tempx = xcos * radius + x;
	    	double tempy = ysin * radius + y;
	    	double tx = Math.cos(Math.toRadians(angle+rotation)) * 0.5 + 0.5;
	    	double ty = Math.sin(Math.toRadians(angle+rotation)) * 0.5 + 0.5;

	    	GL11.glTexCoord2d(tx, ty);
	    	GL11.glVertex2d(tempx, tempy);
	    }

	    GL11.glEnd();
	}
	
	public static void RenderCircle(int x, int y, double radius, double pointSeperation, double rotation, Texture texture) 
	{
	    GL11.glEnable(GL11.GL_TEXTURE_2D);
	    GL11.glColor3f(1, 1, 1);
	    
	    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture()); 
	    RenderCircle(x, y, radius, pointSeperation, rotation);
	    
	    GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
	
    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     */
    public static void DrawTextureRegion(int x, int y, int regX, int regY, int regWidth, int regHeight, Color colour, Texture texture)
    {
        /* Vertex positions */
        int vAX = x;
        int vAY = y + regHeight;
        int vBX = x + regWidth;
        int vBY = y + regHeight;
        int vCX = x + regWidth;
        int vCY = y;
        int vDX = x;
        int vDY = y;

        /* Texture coordinates */
        float tAX = (float) (regX) / texture.getAsBufferedImage().getWidth();
        float tAY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
        float tBX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
        float tBY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
        float tCX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
        float tCY = (float) (regY) / texture.getAsBufferedImage().getHeight();
        float tDX = (float) (regX) / texture.getAsBufferedImage().getWidth();
        float tDY = (float) (regY) / texture.getAsBufferedImage().getHeight();
        
        DrawTextureRegion(vAX, vAY, vBX, vBY, vCX, vCY, vDX, vDY, tAX, tAY, tBX, tBY, tCX, tCY, tDX, tDY, colour, texture);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     */
    public static void DrawTextureRegion(int vAX, int vAY, int vBX, int vBY, int vCX, int vCY, int vDX, int vDY, float tAX, float tAY, float tBX, float tBY, float tCX, float tCY, float tDX, float tDY, Color colour, Texture texture)
    {
    	if(texture.getAsTexture() != 0) 
		{
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			
			GL11.glColor3f((float) colour.getRed() / 256, (float) colour.getGreen() / 256, (float) colour.getBlue() / 256);
			
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
			
			GL11.glBegin(GL11.GL_QUADS);
			
			GL11.glTexCoord2f(tAX, tAY);
			GL11.glVertex2d(vAX, vAY);
			GL11.glTexCoord2f(tBX, tBY);
			GL11.glVertex2d(vBX, vBY);
			GL11.glTexCoord2f(tCX, tCY);
			GL11.glVertex2d(vCX, vCY);
			GL11.glTexCoord2f(tDX, tDY);
			GL11.glVertex2d(vDX, vDY);
			
			GL11.glEnd();
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
    }
}