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
		GL11.glOrtho(0, 800, 0, 600, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
	
	public static void Tick()
	{
		GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		
		GL11.glColor3f(1.0f, 1.0f, 1.0f);
		GL11.glBegin(GL11.GL_TRIANGLES);
		GL11.glVertex2f(200, 200);
		GL11.glVertex2f(400, 200);
		GL11.glVertex2f(300, 400);
		GL11.glEnd();
	}
	
}