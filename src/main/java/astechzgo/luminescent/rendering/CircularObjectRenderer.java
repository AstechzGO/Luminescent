package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class CircularObjectRenderer implements IObjectRenderer {
	private Color colour = new Color(0, 0, 0, 0);

	protected final int pointSeperation;
	protected final double radius;

	protected WindowCoordinates coordinates;

	protected int scaledRadius;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

	protected Texture texture;

	protected double rotation = 0.0;
	
	protected Matrix4f model = new Matrix4f();

	protected boolean doLighting = true;

    public CircularObjectRenderer(WindowCoordinates coordinates, double radius) {
		this(coordinates, radius, 1);
	}

	public CircularObjectRenderer(WindowCoordinates coordinates, double radius, int pointSeperation) {
		this.coordinates = coordinates;

		this.radius = radius;

		this.pointSeperation = pointSeperation;
	}

	public CircularObjectRenderer(WindowCoordinates coordinates, double radius, Texture texture) {
		this(coordinates, radius, 1, texture);
	}

	public CircularObjectRenderer(WindowCoordinates coordinates, double radius, int pointSeperation, Texture texture) {
		this(coordinates, radius, pointSeperation);

		this.texture = texture;
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
		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
		
        ScaledWindowCoordinates loc = new ScaledWindowCoordinates(coordinates);
        Vector3f location = new Vector3f((float)loc.getScaledWindowCoordinatesX() + DisplayUtils.widthOffset, (float)loc.getScaledWindowCoordinatesY()  + DisplayUtils.heightOffset, 0.0f);
        
        Quaternionf rotate = new Quaternionf().rotateZ((float) Math.toRadians(rotation));

		this.model = new Matrix4f().translation(location).rotateAround(rotate, 0, 0, 0).scale((float) (1.0 / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)));
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
				(casted.a.getWindowCoordinatesX()), 
				(casted.b.getWindowCoordinatesX())
			};

			double[] yQuads = { 
				(casted.d.getWindowCoordinatesY()), 
				(casted.a.getWindowCoordinatesY()) 
			};

			boolean[][] quadrant = new boolean[2][2];
			// Clear quadrant flags
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					quadrant[x][y] = false;

			// Determine quadrant of each corner
			for (int x = 0; x < 2; x++) {
				int quadX = (xQuads[x] >= getCoordinates().getWindowCoordinatesX()) ? 0 : 1;
				for (int y = 0; y < 2; y++) {
					int quadY = (yQuads[y] >= getCoordinates().getWindowCoordinatesY()) ? 0 : 1;
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
					float dx = (float) (xQuads[x] - getCoordinates().getWindowCoordinatesX());
					for (int y = 0; y < 2; y++) {
						float dy = (float) (yQuads[y] - getCoordinates().getWindowCoordinatesY());
						if (dx * dx + dy * dy <= radiusSquared)
							collision = true;
					}
				}
				break;
			case 2: // Check for intersection between rectangle and bounding box
					// of the circle
				boolean intersectX = !(xQuads[1] < getCoordinates().getWindowCoordinatesX() - this.radius || xQuads[0] > getCoordinates().getWindowCoordinatesX() + this.radius);
				boolean intersectY = !(yQuads[1] < getCoordinates().getWindowCoordinatesX() - this.radius || yQuads[0] > getCoordinates().getWindowCoordinatesX() + this.radius);
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

			int a = (int) (getCoordinates().getWindowCoordinatesY() - casted.getCoordinates().getWindowCoordinatesY());
			int b = (int) (getCoordinates().getWindowCoordinatesX()- casted.getCoordinates().getWindowCoordinatesX());

			double c = Math.sqrt((a * a) + (b * b));

            return c < (this.radius + casted.radius);
		} 
		else {
			return false;
		}
	}

	@Override
	public boolean doesContain(double x, double y) {
		double a = getCoordinates().getWindowCoordinatesY() - y;
		double b = getCoordinates().getWindowCoordinatesX() - x;

		double c = Math.sqrt((a * a) + (b * b));

        return c < (this.radius);
	}

	@Override
	public WindowCoordinates getCoordinates() {
		return coordinates;
	}

	@Override
	public void setCoordinates(WindowCoordinates coordinates) {
		this.coordinates = coordinates;
	}
	
	@Override
	public Matrix4f getModelMatrix() {
	    resize();
	    return model;
	}

	@Override
	public void setDoesLighting(boolean doLighting) {
		this.doLighting = doLighting;
	}

	@Override
	public boolean doesLighting() {
		return doLighting;
	}

	@Override
    public void upload(List<Supplier<Matrix4f>> matrices) {
        RenderingUtils.createCircle(radius, pointSeperation, colour, texture, this::doesLighting, matrices);
    }
}