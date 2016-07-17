package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.geom.Line2D;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class QuadrilateralObjectRenderer implements IObjectRenderer {

	private Color colour = new Color(0, 0, 0);

	protected Texture texture;

	protected double aX;
	protected double aY;
	
	protected double bX;
	protected double bY;
	
	protected double cX;
	protected double cY;
	
	protected double dX;
	protected double dY;

	protected int scaledAX;
	protected int scaledAY;
	
	protected int scaledBX;
	protected int scaledBY;
	
	protected int scaledCX;
	protected int scaledCY;
	
	protected int scaledDX;
	protected int scaledDY;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

	public QuadrilateralObjectRenderer(double aX, double aY, double bX, double bY, double cX, double cY, double dX, double dY, Texture texture) {
		this.texture = texture;
		
		this.aX = aX;
		this.aY = aY;
		
		this.bX = bX;
		this.bY = bY;
		
		this.cX = cX;
		this.cY = cY;
		
		this.dX = dX;
		this.dY = dY;
	}

	public QuadrilateralObjectRenderer(double aX, double aY, double bX, double bY, double cX, double cY, double dX, double dY) {
		this.aX = aX;
		this.aY = aY;
		
		this.bX = bX;
		this.bY = bY;
		
		this.cX = cX;
		this.cY = cY;
		
		this.dX = dX;
		this.dY = dY;
	}

	@Override
	public void render() {
		resize();

		GL11.glColor4f((float) colour.getRed() / 255, (float) colour.getGreen() / 255, (float) colour.getBlue() / 255, (float) colour.getAlpha() / 255);

		if (texture != null) {
			Luminescent.defaultShader.applyShader();
			Luminescent.defaultShader.updateTransMatrix();
			RenderingUtils.RenderQuad(scaledAX, scaledAY, scaledBX, scaledBY, scaledCX, scaledCY, scaledDX, scaledDY, texture);
			Luminescent.defaultShader.withdrawShader();
		} 
		else {
			RenderingUtils.RenderQuad(scaledAX, scaledAY, scaledBX, scaledBY, scaledCX, scaledCY, scaledDX, scaledDY);
		}
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
		int scaledCamX = ((int) Math
				.round((double)((Camera.CAMERA_WIDTH / 2) - Camera.getX()) / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)));
		int scaledCamY = ((int) Math
				.round((double)((Camera.CAMERA_HEIGHT / 2) - Camera.getY()) / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2)));
		
		scaledAX = ((int) Math
				.round(aX / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledAY = (int) Math
				.round(aY / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledBX = ((int) Math
				.round(bX / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledBY = (int) Math
				.round(bY / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledCX = ((int) Math
				.round(cX / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledCY = (int) Math
				.round(cY / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledDX = ((int) Math
				.round(dX / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledDY = (int) Math
				.round(dY / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}

	@Override
	public boolean isTouching(IObjectRenderer object) {
		if (object instanceof QuadrilateralObjectRenderer) {
			QuadrilateralObjectRenderer casted = (QuadrilateralObjectRenderer) object;
			
			if(
					Line2D.linesIntersect(aX, aY, bX, bY, casted.aX, casted.aY, casted.bX, casted.bY) ||	
					Line2D.linesIntersect(aX, aY, bX, bY, casted.bX, casted.bY, casted.cX, casted.cY) ||	
					Line2D.linesIntersect(aX, aY, bX, bY, casted.cX, casted.cY, casted.dX, casted.dY) ||
					Line2D.linesIntersect(aX, aY, bX, bY, casted.dX, casted.dY, casted.aX, casted.aY) ||
					Line2D.linesIntersect(bX, bY, cX, cY, casted.aX, casted.aY, casted.bX, casted.bY) ||	
					Line2D.linesIntersect(bX, bY, cX, cY, casted.bX, casted.bY, casted.cX, casted.cY) ||	
					Line2D.linesIntersect(bX, bY, cX, cY, casted.cX, casted.cY, casted.dX, casted.dY) ||
					Line2D.linesIntersect(bX, bY, cX, cY, casted.dX, casted.dY, casted.aX, casted.aY) ||
					Line2D.linesIntersect(cX, cY, dX, dY, casted.aX, casted.aY, casted.bX, casted.bY) ||	
					Line2D.linesIntersect(cX, cY, dX, dY, casted.bX, casted.bY, casted.cX, casted.cY) ||	
					Line2D.linesIntersect(cX, cY, dX, dY, casted.cX, casted.cY, casted.dX, casted.dY) ||
					Line2D.linesIntersect(cX, cY, dX, dY, casted.dX, casted.dY, casted.aX, casted.aY) ||
					Line2D.linesIntersect(dX, dY, aX, aY, casted.aX, casted.aY, casted.bX, casted.bY) ||	
					Line2D.linesIntersect(dX, dY, aX, aY, casted.bX, casted.bY, casted.cX, casted.cY) ||	
					Line2D.linesIntersect(dX, dY, aX, aY, casted.cX, casted.cY, casted.dX, casted.dY) ||
					Line2D.linesIntersect(dX, dY, aX, aY, casted.dX, casted.dY, casted.aX, casted.aY)
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
				int quadX = (xQuads[x] >= casted.x) ? 0 : 1;
				for (int y = 0; y < 2; y++) {
					int quadY = (yQuads[y] >= casted.y) ? 0 : 1;
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
					float dx = (float) (xQuads[x] - casted.x);
					for (int y = 0; y < 2; y++) {
						float dy = (float) (yQuads[y] - casted.y);
						if (dx * dx + dy * dy <= radiusSquared)
							collision = true;
					}
				}
				break;
			case 2: // Check for intersection between rectangle and bounding box
					// of the circle
				boolean intersectX = !(xQuads[1] < casted.x - casted.radius || xQuads[0] > casted.x + casted.radius);
				boolean intersectY = !(yQuads[1] < casted.x - casted.radius || yQuads[0] > casted.x + casted.radius);
				if (intersectX && intersectY)
					collision = true;
				break;
			default: // Anything else is a collision
				collision = true;
			}
			return collision;
		} 
		else if (object instanceof MultipleObjectRenderer) {
			MultipleObjectRenderer casted = (MultipleObjectRenderer) object;

			for (IObjectRenderer i : casted.getAll()) {
				if (this.isTouching(i))
					return true;
			}

			return false;
		} 
		return false;
	}

	@Override
	public boolean doesContain(double x, double y) {
		boolean b1, b2, b3;

		double aX = this.aX;
		double aY = this.aY;
		
		double bX = this.bX;
		double bY = this.bY;
		
		double cX = this.cX;
		double cY = this.cY;
		
	    b1 = ((x - bX) * (aY - bY) - (aX - bX) * (y - bY)) < 0.0f;
	    b2 = ((x - cX) * (bY - cY) - (bX - cX) * (y - cY)) < 0.0f;
	    b3 = ((x - aX) * (cY - aY) - (cX - aX) * (y - aY)) < 0.0f;

	    boolean a = ((b1 == b2) && (b2 == b3));
	    

		aX = this.aX;
		aY = this.aY;
		
		bX = this.dX;
		bY = this.dY;
		
		cX = this.cX;
		cY = this.cY;
		
	    b1 = ((x - bX) * (aY - bY) - (aX - bX) * (y - bY)) < 0.0f;
	    b2 = ((x - cX) * (bY - cY) - (bX - cX) * (y - cY)) < 0.0f;
	    b3 = ((x - aX) * (cY - aY) - (cX - aX) * (y - aY)) < 0.0f;

	    boolean b = ((b1 == b2) && (b2 == b3));
	    
	    return a || b;
	}

	@Override
	public double getX() {
		return aX;
	}

	@Override
	public double getY() {
		return aY;
	}

	@Override
	public void setX(double x) {
		aX = x;
	}

	@Override
	public void setY(double y) {
		aY = y;
	}
}
