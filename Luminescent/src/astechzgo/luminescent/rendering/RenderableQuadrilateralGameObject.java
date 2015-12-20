package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.geom.Line2D;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class RenderableQuadrilateralGameObject implements IRenderedObject {

	private Color colour = new Color(0, 0, 0);

	protected Texture texture;

	protected int aX;
	protected int aY;
	
	protected int bX;
	protected int bY;
	
	protected int cX;
	protected int cY;
	
	protected int dX;
	protected int dY;

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

	public RenderableQuadrilateralGameObject(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY, Texture texture) {
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

	public RenderableQuadrilateralGameObject(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY) {
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

		GL11.glColor3f((float) colour.getRed() / 256, (float) colour.getGreen() / 256, (float) colour.getBlue() / 256);
		if (texture != null) {
			RenderingUtils.RenderQuad(scaledAX, scaledAY, scaledBX, scaledBY, scaledCX, scaledCY, scaledDX, scaledDY, texture);
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
				.round((double)((1920 / 2) - Camera.getX()) / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)));
		int scaledCamY = ((int) Math
				.round((double)((1080 / 2) - Camera.getY()) / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2)));
		
		scaledAX = ((int) Math
				.round((double) aX / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledAY = (int) Math
				.round((double) aY / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledBX = ((int) Math
				.round((double) bX / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledBY = (int) Math
				.round((double) bY / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledCX = ((int) Math
				.round((double) cX / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledCY = (int) Math
				.round((double) cY / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;
		scaledDX = ((int) Math
				.round((double) dX / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset + scaledCamX;
		scaledDY = (int) Math
				.round((double) dY / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2))
				+ DisplayUtils.heightOffset + scaledCamY;

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}

	@Override
	public boolean isTouching(IRenderedObject object) {
		if (object instanceof RenderableQuadrilateralGameObject) {
			RenderableQuadrilateralGameObject casted = (RenderableQuadrilateralGameObject) object;
			
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
		else if (object instanceof RenderableCircularGameObject) {
			RenderableCircularGameObject casted = (RenderableCircularGameObject) object;

			int[] xQuads = { 
				(aX), 
				(bX) 
			};

			int[] yQuads = {
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
		else if (object instanceof RenderableMultipleRenderedObjects) {
			RenderableMultipleRenderedObjects casted = (RenderableMultipleRenderedObjects) object;

			for (IRenderedObject i : casted.getAll()) {
				if (this.isTouching(i))
					return true;
			}

			return false;
		} 
		return false;
	}

	@Override
	public boolean doesContain(int x, int y) {
		boolean b1, b2, b3;

		int aX = this.aX;
		int aY = this.aY;
		
		int bX = this.bX;
		int bY = this.bY;
		
		int cX = this.cX;
		int cY = this.cY;
		
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
}
