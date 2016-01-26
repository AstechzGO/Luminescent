package astechzgo.luminescent.entity;

public interface LivingEntity extends Entity {	
	public int getHealth();
	public void setHealth(int health);
	
	public boolean isAlive();
}
