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
	
	public static final ArrayList<Projectile> projectiles = new ArrayList<>();

	public static void shoot(Player thePlayer) {
		// Creates Projectile and adds it to array list
		Projectile projectile = getUnused();
		projectile.init(thePlayer.getCoordinates(), thePlayer);
		projectiles.add(projectile);
	}

	public static void checkGameActions(List<Room> rooms) {
		// For every shot render it and keep shooting it forwards
		for(int i = 0; i < projectiles.size(); i++) {
			Projectile m = projectiles.get(i);
			m.fireBullet(getVerticalEdges(rooms), getHorizontalEdges(rooms));
			m.updateRenderer();


			if(!m.isAlive()) {
				// If the bullet is not it the room delete it
				projectiles.remove(i);
			}
		}

		if(Key.KEYS_NEURAL_LOOSE.isKeyDown()) {
			Luminescent.thePlayerNet.retrainNetworks(false);
			Luminescent.theEnemyNet.retrainNetworks(false);
		}
	}
	
	private static Projectile getUnused() {
	    for(Projectile p : Luminescent.projectilePool) {
	        if(!p.isAlive()) {
	            return p;
	        }
	    }
	    
	    int first = Luminescent.projectilePool.size();
	    
	    for(int i = 0; i < 1024; i++) {
	        Projectile p = new Projectile(new GameCoordinates(0, 0));
	        Luminescent.projectilePool.add(p);
	        Vulkan.addMatrices(Luminescent.projectileIndex, p.getRenderer()::getModelMatrix);
	    }
	    
	    Vulkan.recreateCommandAndUniformBuffers();
	    
	    return Luminescent.projectilePool.get(first);
	}
	
	private static List<Double> getVerticalEdges(List<Room> rooms) {
		List<Double> verticalEdges = new ArrayList<>();
	
		for(Room room : rooms) {
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX());
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX() + room.getWidth());
		}
		
		return verticalEdges;
	}
	
	private static List<Double> getHorizontalEdges(List<Room> rooms) {
		List<Double> horizontalEdges = new ArrayList<>();
		
		for(Room room : rooms) {
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ() + room.getHeight());
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ());
		}
		
		return horizontalEdges;
	}
}
