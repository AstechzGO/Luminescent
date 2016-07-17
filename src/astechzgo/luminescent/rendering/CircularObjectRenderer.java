package astechzgo.luminescent.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class CircularObjectRenderer implements IObjectRenderer {
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

	protected Texture texture;

	protected double rotation = 0.0;
	
	public CircularObjectRenderer(double x, double y, double radius) {
		this(x, y, radius, 1);
	}

	public CircularObjectRenderer(double x, double y, double radius, int pointSeperation) {
		this.x = x;
		this.y = y;

		this.radius = radius;

		this.pointSeperation = pointSeperation;
	}

	public CircularObjectRenderer(double x, double y, double radius, Texture texture) {
		this(x, y, radius, 1, texture);
	}

	public CircularObjectRenderer(double x, double y, double radius, int pointSeperation, Texture texture) {
		this(x, y, radius, pointSeperation);

		this.texture = texture;
	}

	@Override
	public void render() {
		resize();

		GL11.glColor3f((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255);

		if (texture != null) {
			Luminescent.defaultShader.applyShader();
			Luminescent.defaultShader.updateTransMatrix();
			RenderingUtils.RenderCircle(scaledX, scaledY, scaledRadius, pointSeperation, rotation, texture);
			Luminescent.defaultShader.withdrawShader();
		}
		else {
			RenderingUtils.RenderCircle(scaledX, scaledY, scaledRadius, pointSeperation, rotation);
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
		int scaledCamX = (int) (Math
				.round((Camera.CAMERA_WIDTH / 2) - Camera.getX()) / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
		int scaledCamY = (int) (Math
				.round((Camera.CAMERA_HEIGHT / 2) - Camera.getY()) / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2));

		scaledX = ((int) Math
				.round( x / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledY = ((int) Math
				.round(y / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2)))
				+ DisplayUtils.heightOffset + scaledCamY;

		scaledRadius = (int) Math
				.round(radius / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}

	@Override
	public void setTexture(Texture texture) {
		this.texture = texture;
	}

	@Override
	public Texture getTexture() {
		return texture;
	}

	@Override
	public boolean isTouching(IObjectRenderer object) {

		if (object instanceof QuadrilateralObjectRenderer) {
			QuadrilateralObjectRenderer casted = (QuadrilateralObjectRenderer) object;

			double[] xQuads = { 
				(casted.aX), 
				(casted.bX)
			};

			double[] yQuads = { 
				(casted.dY), 
				(casted.aY) 
			};

			boolean[][] quadrant = new boolean[2][2];
			// Clear quadrant flags
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					quadrant[x][y] = false;

			// Determine quadrant of each corner
			for (int x = 0; x < 2; x++) {
				int quadX = (xQuads[x] >= this.x) ? 0 : 1;
				for (int y = 0; y < 2; y++) {
					int quadY = (yQuads[y] >= this.y) ? 0 : 1;
					quadrant[quadX][quadY] = true;
				}
			}

			// Count the number of quadrants with at least one corner
			int quadrants = 0;
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					if (quadrant[x][y])
						quadrants++;

			// Detect Collisions
			boolean collision = false;
			switch (quadrants) {
			case 1: // Check each rectangle corner against circle radius
				float radiusSquared = (float) (this.radius * this.radius);
				for (int x = 0; x < 2; x++) {
					float dx = (float) (xQuads[x] - this.x);
					for (int y = 0; y < 2; y++) {
						float dy = (float) (yQuads[y] - this.y);
						if (dx * dx + dy * dy <= radiusSquared)
							collision = true;
					}
				}
				break;
			case 2: // Check for intersection between rectangle and bounding box
					// of the circle
				boolean intersectX = !(xQuads[1] < this.x - this.radius || xQuads[0] > this.x + this.radius);
				boolean intersectY = !(yQuads[1] < this.x - this.radius || yQuads[0] > this.x + this.radius);
				if (intersectX && intersectY)
					collision = true;
				break;
			default: // Anything else is a collision
				collision = true;
			}
			return collision;
		} 
		else if (object instanceof CircularObjectRenderer) {
			CircularObjectRenderer casted = (CircularObjectRenderer) object;

			int a = (int) (this.y - casted.y);
			int b = (int) (this.x - casted.x);

			double c = Math.sqrt((a * a) + (b * b));

			if (c < (this.radius + casted.radius))
				return true;
			else
				return false;
		} 
		else if (object instanceof MultipleObjectRenderer) {
			MultipleObjectRenderer casted = (MultipleObjectRenderer) object;

			for (IObjectRenderer i : casted.getAll()) {
				if (this.isTouching(i))
					return true;
			}

			return false;
		}
		else {
			return false;
		}
	}

	@Override
	public boolean doesContain(double x, double y) {
		double a = this.y - y;
		double b = this.x - x;

		double c = Math.sqrt((a * a) + (b * b));

		if (c < (this.radius))
			return true;
		else
			return false;
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