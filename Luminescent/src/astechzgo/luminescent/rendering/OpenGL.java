package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

/**
 * 
 * @author Tylar
 *
 * OpenGL class to initialize the 2d rendering scene
 */
public class OpenGL
{
	
	public static void InitOpenGL()
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, 800, 0, 480, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	public static void Tick()
	{
		GL11.glClearColor(0.15f, 0.15f, 0.15f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
	}
	
	public static void ChangeResolution(int x, int y)
	{
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, x, 0, y, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
}