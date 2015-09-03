package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.awt.Rectangle;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWvidmode;

import astechzgo.luminescent.rendering.Player;
import astechzgo.luminescent.rendering.Room;
import astechzgo.luminescent.rendering.RoomWalls;
import astechzgo.luminescent.sound.SoundList;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.KeyboardUtils;
import astechzgo.luminescent.utils.LoggingUtils;
import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class Luminescent
{
	
	public static Player thePlayer = new Player();
	public static double moveSpeed = 0.5;
	
	public static long lastMove = System.currentTimeMillis();
	
		public static Room room = new Room();
		public static RoomWalls walls = new RoomWalls();	
		
	public static void Init()
	{	
		TextureList.loadSlickTextures();
		
		SoundManager manager = new SoundManager();
		SoundList.initSoundList(manager);
		
		
		if(Constants.getConstantAsBoolean(Constants.WINDOW_FULLSCREEN)) 
		{	
			
			setDisplayMode(GLFWvidmode.width(DisplayUtils.vidmode),
					GLFWvidmode.height(DisplayUtils.vidmode), true);
		
			try 
			{
				GLFW.glfwSetInputMode(DisplayUtils.getHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
			} 
			catch (Exception e)
			{
				LoggingUtils.logException(LoggingUtils.LOGGER, e);
			}
			
			
		}
		else 
		{
			setDisplayMode(848, 477, false);
		}
	}
	
	public static void Shutdown()
	{
		GLFW.glfwDestroyWindow(DisplayUtils.getHandle());
		System.exit(0);
	}
	
	public static void Tick()
	{
		room.render();
		
		thePlayer.render();
		
		walls.render();
		
		int multiplier = (int) (System.currentTimeMillis() - lastMove);
		lastMove = System.currentTimeMillis();
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_FASTER))
		{
			Luminescent.moveSpeed = 0.88;
		}
		else
		{
			Luminescent.moveSpeed = 0.5;
		}
		
		double speed = Luminescent.moveSpeed * multiplier;
		
		Rectangle box = room.getBox();
		
		if(thePlayer.getPosX() < box.getX())
		{
			thePlayer.setPosX(box.getX() + box.getWidth());
		}
		if(thePlayer.getPosX() > box.getX() + box.getWidth())
		{
			thePlayer.setPosX(box.getX());
		}
		
		if(thePlayer.getPosY() < box.getY())
		{
			thePlayer.setPosY(box.getY() + box.getHeight());
		}
		if(thePlayer.getPosY() > box.getY() + box.getHeight())
		{
			thePlayer.setPosY(box.getY());
		}
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
		{
			thePlayer.setPosY(thePlayer.getPosY() + speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN))
		{
			thePlayer.setPosY(thePlayer.getPosY() - speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT))
		{
			thePlayer.setPosX(thePlayer.getPosX() + speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT))
		{
			thePlayer.setPosX(thePlayer.getPosX() - speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_EXIT))
		{
			Shutdown();
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_FULLSCREEN))
		{
			if(DisplayUtils.isFullscreen()) 
			{
				setDisplayMode(848, 477, false);
				KeyboardUtils.resetKeys();
			}
			else 
			{
				setDisplayMode(GLFWvidmode.width(DisplayUtils.vidmode),
						GLFWvidmode.height(DisplayUtils.vidmode), true);
				KeyboardUtils.resetKeys();
			}
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_SCREENSHOT))
		{
			File dir = newFile("screenshots/");
			dir = new File(dir.getAbsolutePath());
			
			if(!dir.exists() || !dir.isDirectory()) 
			{
				dir.mkdir();
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			Date dt = new Date();
			String S = sdf.format(dt);

			try 
			{
				DisplayUtils.takeScreenshot(newFile("screenshots/" + S + ".png"));
			} 
			catch (Exception e) 
			{
				LoggingUtils.logException(LoggingUtils.LOGGER, e);
			}
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_NEXTWINDOW))
		{
			if(GLFW.glfwGetMonitors().capacity() > 1)
				DisplayUtils.nextMonitor();
		}
		
		ControllerUtils.updateJoysticks();
	}	
}