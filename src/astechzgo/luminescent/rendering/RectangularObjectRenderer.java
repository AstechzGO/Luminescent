package astechzgo.luminescent.rendering;

import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;

public class RectangularObjectRenderer extends QuadrilateralObjectRenderer {

	protected Texture texture;

	protected double x;
	protected double y;

	protected double width;
	protected double height;

	protected int scaledX;
	protected int scaledY;

	protected int scaledWidth;
	protected int scaledHeight;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

	public RectangularObjectRenderer(double x, double y, double width, double height, Texture texture) {
		super(x, y + height, x + width, y + height, x + width, y, x, y, texture);
		
		this.texture = texture;

		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;
	}

	public RectangularObjectRenderer(double x, double y, double width, double height) {
		super(x, y + height, x + width, y + height, x + width, y, x, y);
		
		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;
	}
	public void render() {
		super.aX = x;
		super. aY = y + height;
		super .bX = x + width;
		super .bY = y + height;
		super.cX = x + width;
		super.cY = y;
		super.dX = x;
		super.dY = y;
				
		super.render();
		
	}

	@Override
	public double getX() {
		return x;
	}

	@Override
	public double getY() {
		return y;
	}

	@Override
	public void setX(double x) {
		this.x = x;
	}

	@Override
	public void setY(double y) {
		this.y = y;
	}
}
