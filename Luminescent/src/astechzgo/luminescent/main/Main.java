package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.awt.Toolkit;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import astechzgo.luminescent.rendering.OpenGL;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.utils.DisplayUtils;

public class Main
{
	public static void main(String[] args)
	{
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
		try
		{
			initSound();
			
			setDisplayMode((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
					(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(), true);
			DisplayUtils.setIcons(
				new String[] {"icon_16x16","icon_32x32","icon_64x64","icon_128x128"}, this
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
		OpenGL.ChangeResolution(DisplayUtils.SCREEN_WIDTH, DisplayUtils.SCREEN_HEIGHT);	// Personal
		
		Luminescent.Init();
	}
	
	public void Shutdown()
	{
		Luminescent.Shutdown();
		
		Display.destroy();
	}
	
	/**
	 * Render everything before handling input
	 */
	public void Tick()
	{
		Display.update();
		
		OpenGL.Tick();
		
		Luminescent.Tick();	// Add in delta time at some point
	}
	
	public void initSound() {
		SoundManager manager = new SoundManager();
		manager.loadSound("Beethoven's 5th Symphony", false); //test TODO: Replace sound
	}
	
}