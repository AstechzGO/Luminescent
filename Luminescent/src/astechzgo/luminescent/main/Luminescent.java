package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.gameobject.RoomWalls;
import astechzgo.luminescent.keypress.KeyPressGameplay;
import astechzgo.luminescent.keypress.KeyPressUtils;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.sound.SoundList;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.textures.Animation;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;

public class Luminescent
{
	
	public static Player thePlayer = new Player();
	public static double moveSpeed = 0.5;
	
	
	public static double lastDelta = GLFW.glfwGetTime() * 1000;
	
	public static Room room = new Room();
	public static RoomWalls walls = new RoomWalls();	
		
	public static void Init()
	{	
		TextureList.loadSlickTextures();

		SoundManager manager = new SoundManager();
		SoundList.initSoundList(manager);
		
		thePlayer.setTexture(new Animation("pacman.frame", 20));
		
		if(Constants.getConstantAsBoolean(Constants.WINDOW_FULLSCREEN)) 
		{	
			setDisplayMode(DisplayUtils.vidmode.width(),
					DisplayUtils.vidmode.height(), true);
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
		Camera.setX((int) thePlayer.getPosX());
		Camera.setY((int) thePlayer.getPosY());
		
		room.render();
		walls.render();
		
		KeyPressUtils.checkUtils();
		KeyPressGameplay.checkGameActions(thePlayer, room);
		
		thePlayer.move(room);
		thePlayer.render();
		ControllerUtils.updateJoysticks();
	}
}