package astechzgo.luminescent.gameobject;

import java.awt.Color;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.worldloader.JSONWorldLoader;

public class Room extends RectangularObjectRenderer {

	public Room(JSONWorldLoader loader) {
		super(new WindowCoordinates(loader.getCoordinates()), loader.getWidth(), loader.getHeight());
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
	
	public WindowCoordinates getCoordinates() {
		return super.coordinates;
	}
	
	public double getWidth() {
		return super.width;
	}
	
	public double getHeight() {
		return super.height;
	}
}
