package astechzgo.luminescent.entity;

import astechzgo.luminescent.rendering.RenderableRectangularGameObject;
import astechzgo.luminescent.textures.Texture;

public class RectangularEntity extends RenderableRectangularGameObject implements Entity {

	public RectangularEntity(double x, double y, double width, double height) {
		super(x, y, width, height);
		// TODO Auto-generated constructor stub
	}

	public RectangularEntity(double x, double y, double width, double height, Texture texture) {
		super(x, y, width, height, texture);
		// TODO Auto-generated constructor stub
	}
}
