package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.rendering.Projectile;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.Player;
import astechzgo.luminescent.rendering.Room;
import astechzgo.luminescent.rendering.RoomWalls;
import astechzgo.luminescent.sound.SoundList;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.textures.Animation;
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
	
	public static ArrayList<Projectile> projectiles = new ArrayList<>();
	
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
		
		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		lastDelta = GLFW.glfwGetTime() * 1000;
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_FASTER))
		{
			Luminescent.moveSpeed = 0.88;
		}
		else
		{
			Luminescent.moveSpeed = 0.5;
		}
		
		double speed = Luminescent.moveSpeed * delta;
		
		double angle = thePlayer.setRotation();		
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
		{
			if(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + room.getWidth() - thePlayer.getRadius());	
			else if(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + thePlayer.getRadius());
			else
				thePlayer.setPosX(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + room.getHeight() - thePlayer.getRadius());
			else if(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + thePlayer.getRadius());
			else
				thePlayer.setPosY(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN))
		{
			speed = -speed;
			if(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + room.getWidth() - thePlayer.getRadius());		
			else if(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + thePlayer.getRadius());
			else
				thePlayer.setPosX(thePlayer.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + room.getHeight() - thePlayer.getRadius());
			else if(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + thePlayer.getRadius());
			else
				thePlayer.setPosY(thePlayer.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT))
		{
			if(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + room.getWidth() - thePlayer.getRadius());
			else if(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)) <= room.getPosX() + thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + thePlayer.getRadius());
			else
				thePlayer.setPosX(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)));
			
			if(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + room.getHeight() - thePlayer.getRadius());
			else if(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)) <= room.getPosY() + thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + thePlayer.getRadius());
			else
				thePlayer.setPosY(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT))
		{
			speed = -speed;
			if(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + room.getWidth() - thePlayer.getRadius());		
			else if(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)) <= room.getPosX() + thePlayer.getRadius())
				thePlayer.setPosX(room.getPosX() + thePlayer.getRadius());
			else
				thePlayer.setPosX(thePlayer.getPosX() + speed * Math.sin(Math.toRadians(angle)));
			
			if(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + room.getHeight() - thePlayer.getRadius());
			else if(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)) <= room.getPosY() + thePlayer.getRadius())
				thePlayer.setPosY(room.getPosY() + thePlayer.getRadius());
			else
				thePlayer.setPosY(thePlayer.getPosY() - speed * Math.cos(Math.toRadians(angle)));
		}

		
		/*if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
		{

			if((thePlayer.getPosY() + speed) >= room.getPosY() + room.getHeight() - thePlayer.getRadius()) {
				thePlayer.setPosY(room.getPosY() + room.getHeight() - thePlayer.getRadius());
				
			}
			else
				thePlayer.setPosY(thePlayer.getPosY() + speed);
		}
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN))
		{
		
			if((thePlayer.getPosY() - speed) <= room.getPosY() + thePlayer.getRadius()){
				thePlayer.setPosY(room.getPosY() + thePlayer.getRadius());
				
			}
			else
				thePlayer.setPosY(thePlayer.getPosY() - speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT))
		{
	
			if((thePlayer.getPosX() + speed) >= room.getPosX() + room.getWidth() - thePlayer.getRadius()) {
				thePlayer.setPosX(room.getPosX() + room.getWidth() - thePlayer.getRadius());
			
			}
			else
				thePlayer.setPosX(thePlayer.getPosX() + speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT))
		{
		
			if((thePlayer.getPosX() - speed) <= room.getPosX() + thePlayer.getRadius()) {
				thePlayer.setPosX(room.getPosX() + thePlayer.getRadius());
		
			}
			else
				thePlayer.setPosX(thePlayer.getPosX() - speed);
		}*/
		if(KeyboardUtils.isKeyDown(Constants.KEYS_ACTION_SHOOT)) {
		// Creates Projectile and adds it to array list
		Projectile projectile = new Projectile((int)thePlayer.getPosX(),(int) thePlayer.getPosY());
		projectiles.add(projectile);
						
			}
		// For every shot render it and keep shooting it forwards
		for(int i = 0; i < projectiles.size(); i++){
		    Projectile m = (Projectile) projectiles.get(i);
		    m.fireBullet();
		   
		        if(room.doesContain((int)m.getX(), (int)m.getY())){
		         	// If the bullet is in the room render it
		            m.render();
		
		        }
		        else if(!room.doesContain((int)m.getX(),(int)m.getY())) {
		        	// If the bullet is not it the room delete it
		            projectiles.remove(i);
		        }
		  
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
				setDisplayMode(DisplayUtils.vidmode.width(),
						DisplayUtils.vidmode.height(), true);
				KeyboardUtils.resetKeys();
			}
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_SCREENSHOT))
		{
			File dir = newFile("screenshots/");
			
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
				LoggingUtils.printException(e);
			}
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_UTIL_NEXTWINDOW))
		{
			if(GLFW.glfwGetMonitors().capacity() > 1)
				DisplayUtils.nextMonitor();
		}
		thePlayer.render();
		ControllerUtils.updateJoysticks();
	}	
}