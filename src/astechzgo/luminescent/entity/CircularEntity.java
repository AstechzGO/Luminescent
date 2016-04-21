package astechzgo.luminescent.entity;

import astechzgo.luminescent.rendering.RenderableCircularGameObject;
import astechzgo.luminescent.textures.Texture;

public class CircularEntity extends RenderableCircularGameObject implements Entity {

	public CircularEntity(double x, double y, double radius) {
		super(x, y, radius);
		// TODO Auto-generated constructor stub
	}

	public CircularEntity(double x, double y, double radius, Texture texture) {
		super(x, y, radius, texture);
		// TODO Auto-generated constructor stub
	}
	
	public CircularEntity(double x, double y, double radius, int pointSeperation) {
		super(x, y, radius, pointSeperation);
		// TODO Auto-generated constructor stub
	}

	public CircularEntity(double x, double y, double radius, int pointSeperation, Texture texture) {
		super(x, y, radius, pointSeperation, texture);
		// TODO Auto-generated constructor stub
	}
}
