package astechzgo.luminescent.keypress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.entity.Projectile;
import astechzgo.luminescent.gameobject.Room;

import static astechzgo.luminescent.keypress.Key.*;

public class KeyPressGameplay {
	
	public static ArrayList<Projectile> projectiles = new ArrayList<>();
	private static double lastShot;
	
	public static void checkGameActions(Player thePlayer, List<Room> rooms) {
		
		double deltaShot = (GLFW.glfwGetTime() * 1000) - lastShot;
		
		if(KEYS_ACTION_SHOOT.isKeyDown() && deltaShot > 250) {
			// Creates Projectile and adds it to array list
			Projectile projectile = new Projectile(thePlayer.getPosX(), thePlayer.getPosY());
			projectiles.add(projectile);	
			
			lastShot = (GLFW.glfwGetTime() * 1000);
		}
		
		// For every shot render it and keep shooting it forwards
		for(int i = 0; i < projectiles.size(); i++) {
			Projectile m = projectiles.get(i);
			m.fireBullet(getVerticalEdges(rooms), getHorizontalEdges(rooms));
			
			if(m.isAlive()) {
				// If the bullet is in the room render it
				m.render();
			}
			else {
				// If the bullet is not it the room delete it
				projectiles.remove(i);
			}
		}
	}
	
	private static List<Double> getVerticalEdges(List<Room> rooms) {
		List<Double> verticalEdges = new ArrayList<Double>();
	
		for(Room room : rooms) {
			verticalEdges.add(room.getPosX());
			verticalEdges.add(room.getPosX() + room.getWidth());
		}
		
		return verticalEdges;
	}
	
	private static List<Double> getHorizontalEdges(List<Room> rooms) {
		List<Double> horizontalEdges = new ArrayList<Double>();
		
		for(Room room : rooms) {
			horizontalEdges.add(room.getPosY() + room.getHeight());
			horizontalEdges.add(room.getPosY());
		}
		
		return horizontalEdges;
	}
}
