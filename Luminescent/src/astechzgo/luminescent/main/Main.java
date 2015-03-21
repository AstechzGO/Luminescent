package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

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
		
		while(!Display.isCloseRequested())
		{
			Tick();
		}
		
		Shutdown();
	}
	
	public void Init()
	{
		LoggingUtils.configureRobotLogger();
		LoggingUtils.LOGGER.info("Hey!");
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