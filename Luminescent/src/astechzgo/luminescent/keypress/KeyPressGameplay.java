package astechzgo.luminescent.keypress;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.entity.Player;
import astechzgo.luminescent.entity.Projectile;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.KeyboardUtils;

public class KeyPressGameplay {
	
	public static ArrayList<Projectile> projectiles = new ArrayList<>();
	private static double lastShot;

	public static void checkGameActions(Player thePlayer, List<Room> rooms) {
		
		double deltaShot = (GLFW.glfwGetTime() * 1000) - lastShot;
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_ACTION_SHOOT) && deltaShot > 250) {
			// Creates Projectile and adds it to array list
			Projectile projectile = new Projectile((int)thePlayer.getPosX(),(int) thePlayer.getPosY());
			projectiles.add(projectile);	
			
			lastShot = (GLFW.glfwGetTime() * 1000);
		}
		
		// For every shot render it and keep shooting it forwards
		 boolean killProjectile = false;
		for(int i = 0; i < projectiles.size(); i++) {
			Projectile m = (Projectile) projectiles.get(i);
			m.fireBullet();
				for(int o = 0; o < rooms.size(); o++) {
			if(rooms.get(o).doesContain((int)m.getX(), (int)m.getY())) {
				// If the bullet is in the room render it
				killProjectile = true;
				m.render();
			}
			else if(!rooms.get(o).doesContain((int)m.getX(),(int)m.getY())) {
				// If the bullet is not it the room delete it
		
			}
				}
				if(!killProjectile) {
					projectiles.remove(i);
				}
		}
	}
}
