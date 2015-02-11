package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

public abstract class GameRenderedObject implements IRenderedObject
{
	public static final double RADIAN = 0.01745329251994329576923690768489d;
	
	public void RenderCircle(double x, double y, double radius)
	{
		RenderCircle(x, y, radius, 1);
	}
	
	public void RenderCircle(double x, double y, double radius, int pointSeperation)
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