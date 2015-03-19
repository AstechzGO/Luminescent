package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import astechzgo.luminescent.rendering.OpenGL;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.DisplayUtils;

public class Main
{
	public static void main(String[] args)
	{
		
		/*
		 * When not deployed, this game only supports windows in the Eclipse IDE
		 * 
		 * When deployed, this game can be run as an executable JAR file in
		 * Windows, Linux, or OS X
		 */
		if(!(System.getProperty("os.name").toLowerCase().contains("win")
				|| System.getProperty("os.name").toLowerCase().contains("unix")
				|| System.getProperty("os.name").toLowerCase().contains("linux")
				|| System.getProperty("os.name").toLowerCase().contains("mac")
				))
		{
			System.err.println("The current platform is not supported");
			System.exit(-1);
		}
		
		new Main().Run();
	}
	
	public void Run()
	{
		Init();
		
		while(!Display.isCloseRequested())
		{
			Tick();
		}
		
		Shutdown();
	}
	
	public void Init()
	{
		TextureList.loadNonSlickTextures();
		try
		{
			setDisplayMode(DisplayUtils.SCREEN_WIDTH, DisplayUtils.SCREEN_HEIGHT, true);
			DisplayUtils.setIcons(
				new String[] {"icons.icon_16x16","icons.icon_32x32","icons.icon_64x64","icons.icon_128x128"}, this
			);
			Display.create();
		}
		catch (LWJGLException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
		
		Display.setTitle("Luminescent");
		
		OpenGL.InitOpenGL();
		
		Luminescent.Init();
	}
	
	public void Shutdown()
	{
		Luminescent.Shutdown();
		
		Display.destroy();
		System.exit(0);
	}
	
	/**
	 * Render everything before handling input
	 */
	public void Tick()
	{
		Display.update();
		
		OpenGL.Tick();
		
		Luminescent.Tick();
	}
	
	// 2994
}