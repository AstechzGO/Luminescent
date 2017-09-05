package astechzgo.luminescent.entity;

import astechzgo.luminescent.coordinates.GameCoordinates;

public abstract class LivingEntity implements Entity {	
	
	protected GameCoordinates coordinates;
	
	protected int health;
	protected boolean isAlive;
	
	public int getHealth() {
		return health;
	}
	public void setHealth(int health) {
		this.health = health;
	}
	
	public boolean isAlive() {
		return isAlive;	
	}
	
	public void setAlive(boolean alive) {
		isAlive = alive;
	}
	
	public void setCoordinates(GameCoordinates coordinates) {
	    this.coordinates = coordinates;
	}
	
	public GameCoordinates getCoordinates() {
		return coordinates;
	}
}
