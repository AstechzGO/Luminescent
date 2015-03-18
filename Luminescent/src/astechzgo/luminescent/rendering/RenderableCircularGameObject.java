package astechzgo.luminescent.rendering;

import astechzgo.luminescent.utils.RenderingUtils;

public abstract class RenderableCircularGameObject implements IRenderedObject {
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
		RenderingUtils.RenderCircle(x, y, radius, pointSeperation);
	}
}