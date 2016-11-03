package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;

public class MultipleObjectRenderer implements IObjectRenderer {

	private Color colour = new Color(0, 0, 0);
	
	private List<IObjectRenderer> objects;
	
	public MultipleObjectRenderer(List<IObjectRenderer> objects) {
		this.objects = objects;
	}

	@Override
	public void render() {
		for(IObjectRenderer object : objects) {
			GL11.glColor4f((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255, (float) colour.getAlpha() / 255);
			object.render();
		}

	}
	
	@Override
	public void setColour(Color colour) {
		this.colour = colour;
	}

	@Override
	public Color getColour() {
		return colour;
	}

	@Override
	public void resize() {
		//Redundant
	}
	
	@Override
	public void setTexture(Texture texture) {
		//Redundant
	}

	@Override
	public Texture getTexture() {
		//Redundant
		return null;
	}

	@Override
	public boolean isTouching(IObjectRenderer object) {
		for(IObjectRenderer i : objects) {
			if(i.isTouching(object)) {
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean doesContain(double x, double y) {
		for(IObjectRenderer i : objects) {
			if(i.doesContain(x, y)) {
				return true;
			}
		}
		
		return false;
	}
	
	public List<IObjectRenderer> getAll() { 
		return objects;
	}

	@Override
	public WindowCoordinates getCoordinates() {
		return new WindowCoordinates(0, 0);
	}

	@Override
	public void setCoordinates(WindowCoordinates coordinates) {
		// Redundant
	}
}
