package astechzgo.luminescent.entity;

import astechzgo.luminescent.rendering.RenderableRectangularGameObject;
import astechzgo.luminescent.textures.Texture;

public class RectangularEntity extends RenderableRectangularGameObject implements Entity {

	public RectangularEntity(int x, int y, int width, int height) {
		super(x, y, width, height);
		// TODO Auto-generated constructor stub
	}

	public RectangularEntity(int x, int y, int width, int height, Texture texture) {
		super(x, y, width, height, texture);
		// TODO Auto-generated constructor stub
	}
	public int health = 5;
	public int damage = 2;
	@Override
	public void SetHealth(int health) {
		// TODO Auto-generated method stub
		this.health = health;
	}

	@Override
	public int GetHealth() {
		// TODO Auto-generated method stub
		return health;
	}

	@Override
	public boolean IsDead() {
		// TODO Auto-generated method stub
		if(health <= 0) {
			return true;
		} else
			return false;
	}
}
