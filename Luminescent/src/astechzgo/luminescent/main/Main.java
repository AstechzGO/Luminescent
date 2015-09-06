package astechzgo.luminescent.main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_TRUE;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.rendering.OpenGL;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.LoggingUtils;
import astechzgo.luminescent.utils.SystemUtils;

public class Main
{
	public static void main(String[] args)
	{
		SystemUtils.doOSSetUp();
		
		new Main().Run();
	}
	
	public void Run()
	{
		Init();
		
		while(glfwWindowShouldClose(DisplayUtils.getHandle()) != GL_TRUE)
		{
			Tick();
		}
		
		Shutdown();
	}
	
	public void Init()
	{
		Constants.readConstantPropertiesFromFile();
		LoggingUtils.configureLogger();

		TextureList.loadNonSlickTextures();
		try
		{
			//DisplayUtils.setIcons(
			//	new String[] {"icons.icon_16x16","icons.icon_32x32","icons.icon_64x64","icons.icon_128x128"}, this
			//);
			DisplayUtils.create();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		DisplayUtils.displayTitle = "Luminescent";
		
		OpenGL.InitOpenGL();
		
		Luminescent.Init();
	}
	
	public void Shutdown()
	{
		Luminescent.Shutdown();
		
		GLFW.glfwDestroyWindow(DisplayUtils.getHandle());
		System.exit(0);
	}
	
	/**
	 * Render everything before handling input
	 */
	public void Tick()
	{
		GLFW.glfwSwapBuffers(DisplayUtils.getHandle());
		glfwPollEvents();
		
		OpenGL.Tick();
		
		Luminescent.Tick();
	}
	
	// 2994
}