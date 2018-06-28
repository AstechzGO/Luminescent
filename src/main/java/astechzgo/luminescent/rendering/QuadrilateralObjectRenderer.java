package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.geom.Line2D;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class QuadrilateralObjectRenderer implements IObjectRenderer {

	private Color colour = new Color(0, 0, 0, 0);

	protected Texture texture;

	protected WindowCoordinates a;
	protected WindowCoordinates b;
	protected WindowCoordinates c;
	protected WindowCoordinates d;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	
	protected Matrix4f model = new Matrix4f();

	protected boolean doLighting = true;

	public QuadrilateralObjectRenderer(WindowCoordinates a,WindowCoordinates b, WindowCoordinates c, WindowCoordinates d, Texture texture) {
		this.texture = texture;
		
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}

	public QuadrilateralObjectRenderer(WindowCoordinates a,WindowCoordinates b, WindowCoordinates c, WindowCoordinates d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
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

        ScaledWindowCoordinates loc = new ScaledWindowCoordinates(this.getCoordinates());
        Vector3f location = new Vector3f((float)loc.getScaledWindowCoordinatesX() + DisplayUtils.widthOffset, (float)loc.getScaledWindowCoordinatesY()  + DisplayUtils.heightOffset, 0.0f);

        Vector3f scale = new Vector3f((((float)DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2) / (float)Camera.CAMERA_WIDTH),
			(((float)DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2) / (float)Camera.CAMERA_HEIGHT), 1.0f);

		this.model = new Matrix4f().translation(location).scale(scale);
	}

	@Override
	public boolean isTouching(IObjectRenderer object) {
		double aX = a.getWindowCoordinatesX();
		double aY = a.getAbsoluteY();
		
		double bX = b.getWindowCoordinatesX();
		double bY = b.getAbsoluteY();
		
		double cX = c.getWindowCoordinatesX();
		double cY = c.getAbsoluteY();
		
		double dX = d.getWindowCoordinatesX();
		double dY = d.getAbsoluteY();
		
		if (object instanceof QuadrilateralObjectRenderer) {
			QuadrilateralObjectRenderer casted = (QuadrilateralObjectRenderer) object;
			
			double castedaX = casted.a.getWindowCoordinatesX();
			double castedaY = casted.a.getAbsoluteY();
			
			double castedbX = casted.b.getWindowCoordinatesX();
			double castedbY = casted.b.getAbsoluteY();
			
			double castedcX = casted.c.getWindowCoordinatesX();
			double castedcY = casted.c.getAbsoluteY();
			
			double casteddX = casted.d.getWindowCoordinatesX();
			double casteddY = casted.d.getAbsoluteY();
			
			if(
					Line2D.linesIntersect(aX, aY, bX, bY, castedaX, castedaY, castedbX, castedbY) ||	
					Line2D.linesIntersect(aX, aY, bX, bY, castedbX, castedbY, castedcX, castedcY) ||	
					Line2D.linesIntersect(aX, aY, bX, bY, castedcX, castedcY, casteddX, casteddY) ||
					Line2D.linesIntersect(aX, aY, bX, bY, casteddX, casteddY, castedaX, castedaY) ||
					Line2D.linesIntersect(bX, bY, cX, cY, castedaX, castedaY, castedbX, castedbY) ||	
					Line2D.linesIntersect(bX, bY, cX, cY, castedbX, castedbY, castedcX, castedcY) ||	
					Line2D.linesIntersect(bX, bY, cX, cY, castedcX, castedcY, casteddX, casteddY) ||
					Line2D.linesIntersect(bX, bY, cX, cY, casteddX, casteddY, castedaX, castedaY) ||
					Line2D.linesIntersect(cX, cY, dX, dY, castedaX, castedaY, castedbX, castedbY) ||	
					Line2D.linesIntersect(cX, cY, dX, dY, castedbX, castedbY, castedcX, castedcY) ||	
					Line2D.linesIntersect(cX, cY, dX, dY, castedcX, castedcY, casteddX, casteddY) ||
					Line2D.linesIntersect(cX, cY, dX, dY, casteddX, casteddY, castedaX, castedaY) ||
					Line2D.linesIntersect(dX, dY, aX, aY, castedaX, castedaY, castedbX, castedbY) ||	
					Line2D.linesIntersect(dX, dY, aX, aY, castedbX, castedbY, castedcX, castedcY) ||	
					Line2D.linesIntersect(dX, dY, aX, aY, castedcX, castedcY, casteddX, casteddY) ||
					Line2D.linesIntersect(dX, dY, aX, aY, casteddX, casteddY, castedaX, castedaY)
					)
			return true;
		}
		else if (object instanceof CircularObjectRenderer) {
			CircularObjectRenderer casted = (CircularObjectRenderer) object;

			double[] xQuads = { 
				(aX), 
				(bX) 
			};

			double[] yQuads = {
				(dY),
				(aY)
			};

			boolean[][] quadrant = new boolean[2][2];
			// Clear quadrant flags
			for (int x = 0; x < 2; x++)
				for (int y = 0; y < 2; y++)
					quadrant[x][y] = false;

			// Determine quadrant of each corner
			for (int x = 0; x < 2; x++) {
				int quadX = (xQuads[x] >= casted.getCoordinates().getWindowCoordinatesX()) ? 0 : 1;
				for (int y = 0; y < 2; y++) {
					int quadY = (yQuads[y] >= casted.getCoordinates().getWindowCoordinatesY()) ? 0 : 1;
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
				float radiusSquared = (float) (casted.radius * casted.radius);
				for (int x = 0; x < 2; x++) {
					float dx = (float) (xQuads[x] - casted.getCoordinates().getWindowCoordinatesX());
					for (int y = 0; y < 2; y++) {
						float dy = (float) (yQuads[y] - casted.getCoordinates().getWindowCoordinatesY());
						if (dx * dx + dy * dy <= radiusSquared)
							collision = true;
					}
				}
				break;
			case 2: // Check for intersection between rectangle and bounding box
					// of the circle
				boolean intersectX = !(xQuads[1] < casted.getCoordinates().getWindowCoordinatesX() - casted.radius || xQuads[0] > casted.getCoordinates().getWindowCoordinatesX() + casted.radius);
				boolean intersectY = !(yQuads[1] < casted.getCoordinates().getWindowCoordinatesX() - casted.radius || yQuads[0] > casted.getCoordinates().getWindowCoordinatesX() + casted.radius);
				if (intersectX && intersectY)
					collision = true;
				break;
			default: // Anything else is a collision
				collision = true;
			}
			return collision;
		} 
		return false;
	}

	@Override
	public boolean doesContain(double x, double y) {
		boolean b1, b2, b3;

		double aX = this.a.getWindowCoordinatesX();
		double aY = this.a.getWindowCoordinatesY();
		
		double bX = this.b.getWindowCoordinatesX();
		double bY = this.b.getWindowCoordinatesY();
		
		double cX = this.c.getWindowCoordinatesX();
		double cY = this.c.getWindowCoordinatesY();
		
	    b1 = ((x - bX) * (aY - bY) - (aX - bX) * (y - bY)) < 0.0f;
	    b2 = ((x - cX) * (bY - cY) - (bX - cX) * (y - cY)) < 0.0f;
	    b3 = ((x - aX) * (cY - aY) - (cX - aX) * (y - aY)) < 0.0f;

	    boolean a = ((b1 == b2) && (b2 == b3));
	    

		aX = this.a.getWindowCoordinatesX();
		aY = this.a.getWindowCoordinatesY();
		
		bX = this.d.getWindowCoordinatesX();
		bY = this.d.getWindowCoordinatesY();
		
		cX = this.c.getWindowCoordinatesX();
		cY = this.c.getWindowCoordinatesY();
		
	    b1 = ((x - bX) * (aY - bY) - (aX - bX) * (y - bY)) < 0.0f;
	    b2 = ((x - cX) * (bY - cY) - (bX - cX) * (y - cY)) < 0.0f;
	    b3 = ((x - aX) * (cY - aY) - (cX - aX) * (y - aY)) < 0.0f;

	    boolean b = ((b1 == b2) && (b2 == b3));
	    
	    return a || b;
	}

	@Override
	public WindowCoordinates getCoordinates() {
		// Same as rectangle (bottom left corner)
		return a;
	}

	@Override
	public void setCoordinates(WindowCoordinates coordinates) {
		// Same as rectangle (bottom left corner)
		// Won't do much except stretch a corner
		this.a = coordinates;
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
        RenderingUtils.createQuad(a, b, c, d, colour, texture, this::doesLighting, matrices);
    }
}
