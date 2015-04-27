package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import astechzgo.luminescent.rendering.Player;
import astechzgo.luminescent.rendering.RenderableQuadrilateralGameObject;
import astechzgo.luminescent.rendering.Room;
import astechzgo.luminescent.rendering.RoomWalls;
import astechzgo.luminescent.sound.SoundList;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.LoggingUtils;
import astechzgo.luminescent.utils.SystemUtils;
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
		
		try 
		{
			Cursor emptyCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
			Mouse.setNativeCursor(emptyCursor);
		} 
		catch (Exception e)
		{
			LoggingUtils.logException(LoggingUtils.LOGGER, e);
		}
		
		SoundManager manager = new SoundManager();
		SoundList.initSoundList(manager);
	}
	
	public static void Shutdown()
	{
		Display.destroy();
		System.exit(0);
	}
	
	public static void Tick()
	{
		if(!Display.isFullscreen()) {
			RenderableQuadrilateralGameObject windowed;
			windowed = new RenderableQuadrilateralGameObject(0, 0, TextureList.findTexture("misc.notFullscreen"));
			windowed.render();
			if(SystemUtils.isKeyDown(Constants.KEYS_UTIL_EXIT))
			{
				Shutdown();
			}
			if(SystemUtils.isKeyDown(Constants.KEYS_UTIL_FULLSCREEN))
			{
				if(Display.isFullscreen()) 
				{
					setDisplayMode(854, 480, false);
				}
				else 
				{
					setDisplayMode((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
							(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(), true);
				}
			}
			return;
		}
		room.render();
		
		thePlayer.render();
		
		walls.render();
		
		int multiplier = (int) (System.currentTimeMillis() - lastMove);
		lastMove = System.currentTimeMillis();
		
		if(SystemUtils.isKeyDown(Constants.KEYS_MOVEMENT_FASTER))
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
		
		if(SystemUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
		{
			thePlayer.setPosY(thePlayer.getPosY() + speed);
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN))
		{
			thePlayer.setPosY(thePlayer.getPosY() - speed);
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT))
		{
			thePlayer.setPosX(thePlayer.getPosX() + speed);
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT))
		{
			thePlayer.setPosX(thePlayer.getPosX() - speed);
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_UTIL_EXIT))
		{
			Shutdown();
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_UTIL_FULLSCREEN))
		{
			if(Display.isFullscreen()) 
			{
				setDisplayMode(854, 480, false);
			}
			else 
			{
				setDisplayMode((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
						(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(), true);
			}
		}
		if(SystemUtils.isKeyDown(Constants.KEYS_UTIL_SCREENSHOT))
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
	}
	
}