package astechzgo.luminescent.gameobject;

import java.awt.Color;

import astechzgo.luminescent.rendering.RenderableRectangularGameObject;
import astechzgo.luminescent.worldloader.JSONWorldLoader;

public class Room extends RenderableRectangularGameObject {

	public Room(JSONWorldLoader loader) {
		super(loader.getX(), loader.getY(), loader.getWidth(), loader.getHeight());
		super.setColour(new Color(0.15f, 0.15f, 0.15f));
	}

	public void setSize(int width, int height) {
		super.width = width;
		super.height = height;
	}

	@Override
	public void render() {
		super.render();
	}
	
	public double getPosX() {
		return super.x;
	}
	
	public double getPosY() {
		return super.y;
	}
	
	public double getWidth() {
		return super.width;
	}
	
	public double getHeight() {
		return super.height;
	}
}
