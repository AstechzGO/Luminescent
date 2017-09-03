package astechzgo.luminescent.keypress;

import static astechzgo.luminescent.keypress.Key.KEYS_ACTION_SHOOT;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.entity.Projectile;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.Vulkan;

public class KeyPressGameplay {
	
	public static ArrayList<Projectile> projectiles = new ArrayList<>();
	private static double lastShot;
	
	public static void checkGameActions(Player thePlayer, List<Room> rooms) {
		
		double deltaShot = (GLFW.glfwGetTime() * 1000) - lastShot;
		
		if(KEYS_ACTION_SHOOT.isKeyDown() && deltaShot > 250) {
			// Creates Projectile and adds it to array list
			Projectile projectile = getUnused();
			projectile.init(thePlayer.getCoordinates());
			projectiles.add(projectile);	
			
			lastShot = (GLFW.glfwGetTime() * 1000);
		}
		
		// For every shot render it and keep shooting it forwards
		for(int i = 0; i < projectiles.size(); i++) {
			Projectile m = projectiles.get(i);
			m.updateRenderer();
			m.fireBullet(getVerticalEdges(rooms), getHorizontalEdges(rooms));
			
			if(!m.isAlive()) {
				// If the bullet is not it the room delete it
				projectiles.remove(i);
			}
		}
	}
	
	private static Projectile getUnused() {
	    for(Projectile p : Luminescent.projectilePool) {
	        if(!p.isAlive()) {
	            return p;
	        }
	    }
	    
	    int first = Luminescent.projectilePool.size();
	    for(int i = 0; i < 32; i++) {
	        Projectile p = new Projectile(new GameCoordinates(0, 0));
	        Luminescent.projectilePool.add(p);
	        p.upload();
	    }
	    
	    Vulkan.recreateBuffers();
	    
	    return Luminescent.projectilePool.get(first);
	}
	
	private static List<Double> getVerticalEdges(List<Room> rooms) {
		List<Double> verticalEdges = new ArrayList<Double>();
	
		for(Room room : rooms) {
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX());
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX() + room.getWidth());
		}
		
		return verticalEdges;
	}
	
	private static List<Double> getHorizontalEdges(List<Room> rooms) {
		List<Double> horizontalEdges = new ArrayList<Double>();
		
		for(Room room : rooms) {
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ() + room.getHeight());
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ());
		}
		
		return horizontalEdges;
	}
}
