package astechzgo.luminescent.entity;

public abstract interface LivingEntity extends Entity {	
	public int getHealth();
	public void setHealth(int health);
	
	public boolean isAlive();
}
