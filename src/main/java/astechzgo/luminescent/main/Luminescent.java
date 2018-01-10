package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import astechzgo.luminescent.entity.AIPlayer;
import astechzgo.luminescent.entity.HumanPlayer;
import astechzgo.luminescent.neuralnetwork.NeuralNet;
import org.joml.Matrix4f;
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
import astechzgo.luminescent.rendering.FPSCalculator;
import astechzgo.luminescent.rendering.QuadrilateralObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.rendering.ResolutionBorderRenderer;
import astechzgo.luminescent.rendering.TextLabelRenderer;
import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.sound.Sound;
import astechzgo.luminescent.text.Font;
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
	public static Player theEnemy;
	public static NeuralNet thePlayerNet;
	public static NeuralNet theEnemyNet;
	
	public static List<Room> rooms;
	
//	public static RectangularObjectRenderer darkness;
	
	public static List<Projectile> projectilePool;
	public static int projectileIndex;
	
	private static TextLabelRenderer fpsText;
	private static FPSCalculator fpsCalc;
	
	public static QuadrilateralObjectRenderer[] resBorders;
	
	public static void Init()
	{	
		TextureList.loadSlickTextures();
		
		Sound.init();
		
		thePlayer = new AIPlayer(new GameCoordinates(Camera.CAMERA_WIDTH / 3, Camera.CAMERA_HEIGHT / 2));
		theEnemy = new AIPlayer(new GameCoordinates(Camera.CAMERA_WIDTH * 2 / 3, Camera.CAMERA_HEIGHT / 2));

		rooms = JSONWorldLoader.loadRooms();
		thePlayer.getRenderer().setTexture(new Animation("player.frame", 16));
		theEnemy.getRenderer().setTexture(new Animation("player.frame", 16));
		//darkness = new RectangularObjectRenderer(new WindowCoordinates(0, 0), Camera.CAMERA_WIDTH, Camera.CAMERA_HEIGHT, TextureList.findTexture("light.darkness"));
		projectilePool = new ArrayList<>(32);
		for(int i = 0; i < 1024; i++) {
		    projectilePool.add(new Projectile(new GameCoordinates(0, 0)));
		}
		
		fpsText = new TextLabelRenderer(new WindowCoordinates(0, 0), new Font(45), "FPS: #####");
		fpsCalc = new FPSCalculator(new DecimalFormat("#####"), 1);
		
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
		    room.upload();
        
        thePlayer.upload();
        theEnemy.upload();
         
        List<Supplier<Matrix4f>> projectileMatrices = new ArrayList<>(projectilePool.size());
        for(Projectile projectile : projectilePool) {
            projectileMatrices.add(projectile.getRenderer()::getModelMatrix);
        }
        
        @SuppressWarnings("unchecked")
        Supplier<Matrix4f>[] projectileMatricesArray = (Supplier<Matrix4f>[]) projectileMatrices.toArray(new Supplier<?>[0]);
        projectilePool.get(0).upload(List.of(projectileMatricesArray));
        projectileIndex = Vulkan.getInstances() - 1;
        
       // darkness.upload();
        
        fpsText.upload();
        
        for(QuadrilateralObjectRenderer resBorder : resBorders) {
            resBorder.upload();
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
	    fpsText.setText("FPS: " + fpsCalc.getFormattedFPS(GLFW.glfwGetTime()));
	    
		Key.updateKeys();
		
		Camera.setCameraCoordinates(thePlayer.getCoordinates());
		
		thePlayer.move(rooms);
		theEnemy.move(rooms);
		
		KeyPressUtils.checkUtils();		
		KeyPressGameplay.checkGameActions(rooms);
		
		ControllerUtils.updateJoysticks();
	}
}