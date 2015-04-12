package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import astechzgo.luminescent.utils.RenderingUtils;

public abstract class RenderableCircularGameObject implements IRenderedObject {
	private Color colour = new Color(0, 0, 0);
	
	protected int pointSeperation;
	protected double radius;
	
	protected double x;
	protected double y;
	
	public RenderableCircularGameObject(double x, double y, double radius) {
		this.x = x;
		this.y = y;
		
		this.radius = radius;
	}
	
	public RenderableCircularGameObject(double x, double y, double radius, int pointSeperation) {
		this.x = x;
		this.y = y;
		
		this.radius = radius;
		
		this.pointSeperation = pointSeperation;
	}
	
	@Override
	public void render() {
		GL11.glColor3f(colour.r, colour.g, colour.b);
		RenderingUtils.RenderCircle(x, y, radius, pointSeperation);
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