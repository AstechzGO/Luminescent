package astechzgo.luminescent.entity;

import java.util.List;

import astechzgo.luminescent.rendering.IRenderedObject;
import astechzgo.luminescent.rendering.RenderableMultipleRenderedObjects;

public class PolygonalEntity extends RenderableMultipleRenderedObjects implements Entity {

	public PolygonalEntity(List<IRenderedObject> objects) {
		super(objects);
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
