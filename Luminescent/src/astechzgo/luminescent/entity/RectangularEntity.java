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
}
