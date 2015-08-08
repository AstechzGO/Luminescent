package astechzgo.luminescent.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class RenderableCircularGameObject implements IRenderedObject {
	private Color colour = new Color(0, 0, 0);
	
	protected int pointSeperation;
	protected double radius;
	
	protected double x;
	protected double y;
	
	protected int scaledX;
	protected int scaledY;
	
	protected int scaledRadius;
	
	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
			
	public RenderableCircularGameObject(double x, double y, double radius) {
		this(x, y, radius, 1);	
	}
	
	public RenderableCircularGameObject(double x, double y, double radius, int pointSeperation) {
		this.x = x;
		this.y = y;
		
		this.radius = radius;
		
		this.pointSeperation = pointSeperation;
	}
	
	@Override
	public void render() {
		resize();
		
		GL11.glColor3f((float)colour.getRed() / 256, (float)colour.getGreen() / 256, (float)colour.getBlue() / 256);
		RenderingUtils.RenderCircle(scaledX, scaledY, scaledRadius, pointSeperation);
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
		scaledX = ((int)Math.round((double)x / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2))) + DisplayUtils.widthOffset;
		scaledY = ((int)Math.round((double)y / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))) + DisplayUtils.heightOffset;
		
		scaledRadius = (int)Math.round((double)radius / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
		
		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}
}