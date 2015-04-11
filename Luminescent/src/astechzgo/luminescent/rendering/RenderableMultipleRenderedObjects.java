package astechzgo.luminescent.rendering;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

public class RenderableMultipleRenderedObjects implements IRenderedObject {

	private Color colour = new Color(0, 0, 0);
	
	private List<IRenderedObject> objects;
	
	public RenderableMultipleRenderedObjects(List<IRenderedObject> objects) {
		this.objects = objects;
	}

	@Override
	public void render() {
		for(IRenderedObject object : objects) {
			GL11.glColor3f(colour.r, colour.g, colour.b);
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
}
