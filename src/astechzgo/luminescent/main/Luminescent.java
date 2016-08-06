package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.keypress.Key;
import astechzgo.luminescent.keypress.KeyPressGameplay;
import astechzgo.luminescent.keypress.KeyPressUtils;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.IObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.shader.ShaderList;
import astechzgo.luminescent.shader.ShaderProgram;
//import astechzgo.luminescent.rendering.LightSource;
import astechzgo.luminescent.sound.Sound;
import astechzgo.luminescent.textures.Animation;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.worldloader.JSONWorldLoader;

public class Luminescent
{
	
	public static Player thePlayer;

	public static double lastDelta;
	
	public static List<Room> rooms;
	
	public static ShaderProgram defaultShader;
	
	public static List<IObjectRenderer> renderingQueue;
	
	public static RectangularObjectRenderer darkness;
		
	public static void Init()
	{	
		TextureList.loadSlickTextures();
		
		Sound.init();
		ShaderList.initShaderList();
		
		thePlayer = new Player();	
		lastDelta = GLFW.glfwGetTime() * 1000;
		rooms = JSONWorldLoader.loadRooms();
		defaultShader = new ShaderProgram(ShaderList.findShader("defaults.defaultVertexShader"), ShaderList.findShader("defaults.defaultPixelShader"));
		thePlayer.getRenderer().setTexture(new Animation("player.frame", 16));
		renderingQueue = new ArrayList<IObjectRenderer>();
		darkness = new RectangularObjectRenderer(new WindowCoordinates(0, 0), Camera.CAMERA_WIDTH, Camera.CAMERA_HEIGHT, TextureList.findTexture("light.darkness"));
		
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
		TextureList.cleanup();
		Animation.cleanup();
	}
	
	public static void Tick()
	{
		Key.updateKeys();
		
		Camera.setCameraCoordinates(thePlayer.getCoordinates());
		

		for(Room room : rooms)
			room.queue();
		
		thePlayer.move(rooms);
		thePlayer.queue();
		
		KeyPressUtils.checkUtils();		
		KeyPressGameplay.checkGameActions(thePlayer, rooms);
		
		darkness.queue();
		
		ControllerUtils.updateJoysticks();
		
		DisplayUtils.renderResolutionBorder();
		
		for(IObjectRenderer object : renderingQueue)
			object.render();
		
		renderingQueue.clear();
	}
}