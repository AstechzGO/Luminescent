package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.List;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.Texture;

public class RenderableMultipleRenderedObjects implements IRenderedObject {

	private Color colour = new Color(0, 0, 0);
	
	private List<IRenderedObject> objects;
	
	public RenderableMultipleRenderedObjects(List<IRenderedObject> objects) {
		this.objects = objects;
	}

	@Override
	public void render() {
		for(IRenderedObject object : objects) {
			GL11.glColor3f((float)colour.getRed() / 256, (float)colour.getGreen() / 256, (float)colour.getBlue() / 256);
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
}
