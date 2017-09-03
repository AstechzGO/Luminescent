package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.entity.Projectile;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.keypress.Key;
import astechzgo.luminescent.keypress.KeyPressGameplay;
import astechzgo.luminescent.keypress.KeyPressUtils;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.IObjectRenderer;
import astechzgo.luminescent.rendering.QuadrilateralObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.rendering.ResolutionBorderRenderer;
import astechzgo.luminescent.rendering.TextLabelRenderer;
import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.sound.Sound;
import astechzgo.luminescent.textures.Animation;
import astechzgo.luminescent.textures.TextureList;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.worldloader.JSONWorldLoader;

public class Luminescent
{
	public static final boolean DEBUG = true;
    
	public static Player thePlayer;

	public static double lastDelta;
	
	public static List<Room> rooms;
	
	public static List<IObjectRenderer> renderingQueue;
	
	public static RectangularObjectRenderer darkness;
	
	public static List<Projectile> projectilePool;
	
	public static QuadrilateralObjectRenderer[] resBorders;
	
	public static TextLabelRenderer text;
		
	public static void Init()
	{	
		TextureList.loadSlickTextures();
		
		Sound.init();
		
		thePlayer = new Player();	
		lastDelta = GLFW.glfwGetTime() * 1000;
		rooms = JSONWorldLoader.loadRooms();
		thePlayer.getRenderer().setTexture(new Animation("player.frame", 16));
		renderingQueue = new ArrayList<IObjectRenderer>();
		darkness = new RectangularObjectRenderer(new WindowCoordinates(0, 0), Camera.CAMERA_WIDTH, Camera.CAMERA_HEIGHT, TextureList.findTexture("light.darkness"));
		projectilePool = new ArrayList<>(32);
		for(int i = 0; i < 32; i++) {
		    projectilePool.add(new Projectile(new GameCoordinates(0, 0)));
		}
		
		resBorders = new QuadrilateralObjectRenderer[] {
		    new ResolutionBorderRenderer(ResolutionBorderRenderer.LEFT_RECTANGLE),
		    new ResolutionBorderRenderer(ResolutionBorderRenderer.RIGHT_RECTANGLE),
		    new ResolutionBorderRenderer(ResolutionBorderRenderer.TOP_RECTANGLE),
		    new ResolutionBorderRenderer(ResolutionBorderRenderer.BOTTOM_RECTANGLE),
		};
		
		if(Constants.getConstantAsBoolean(Constants.WINDOW_FULLSCREEN)) 
		{	
			setDisplayMode(DisplayUtils.vidmode.width(),
					DisplayUtils.vidmode.height(), true);
		}
		else 
		{
			setDisplayMode(848, 477, false);
		}
		
		for(Room room : rooms)
		    room.queue();
        
        thePlayer.queue();
         
        for(Projectile projectile : projectilePool) {
            projectile.queue();
        }
      
        darkness.queue();
        
        
        for(QuadrilateralObjectRenderer resBorder : resBorders) {
            resBorder.queue();
        }
        
        
        for(IObjectRenderer object : renderingQueue) {
            object.upload();
        }
        
        Vulkan.constructBuffers();
	}
	
	public static void Shutdown()
	{
	    DisplayUtils.setIcons(null);
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
		
		thePlayer.move(rooms);
		
		KeyPressUtils.checkUtils();		
		KeyPressGameplay.checkGameActions(thePlayer, rooms);
		
		ControllerUtils.updateJoysticks();
	}
}