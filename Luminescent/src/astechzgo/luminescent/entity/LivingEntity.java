package astechzgo.luminescent.entity;

public abstract class LivingEntity implements Entity {	
	
	protected double x;
	protected double y;
	
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

	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
}
