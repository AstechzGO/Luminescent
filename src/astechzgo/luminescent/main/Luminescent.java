package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.util.List;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.keypress.Key;
import astechzgo.luminescent.keypress.KeyPressGameplay;
import astechzgo.luminescent.keypress.KeyPressUtils;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.sound.Sound;
import astechzgo.luminescent.textures.Animation;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.worldloader.JSONWorldLoader;

public class Luminescent
{
	
	public static Player thePlayer = new Player();

	public static double lastDelta = GLFW.glfwGetTime() * 1000;
	
	public static List<Room> rooms = JSONWorldLoader.loadRooms();
		
	public static void Init()
	{	
		TextureList.loadSlickTextures();
		
		Sound.init();
		
		thePlayer.getRenderer().setTexture(new Animation("player.frame", 16));
		
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
		Callbacks.glfwFreeCallbacks(DisplayUtils.getHandle());
		GLFW.glfwDestroyWindow(DisplayUtils.getHandle());
		Sound.cleanup();
		Animation.cleanup();
	}
	
	public static void Tick()
	{
		Key.updateKeys();
		
		Camera.setX(thePlayer.getPosX());
		Camera.setY(thePlayer.getPosY());
		

		for(Room room : rooms)
			room.render();
		
		thePlayer.move(rooms);
		thePlayer.render();
		
		KeyPressUtils.checkUtils();		
		KeyPressGameplay.checkGameActions(thePlayer, rooms);
		
		ControllerUtils.updateJoysticks();
		
		DisplayUtils.renderResolutionBorder();
	}
}