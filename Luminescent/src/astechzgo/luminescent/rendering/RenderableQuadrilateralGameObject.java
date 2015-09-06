package astechzgo.luminescent.rendering;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class RenderableQuadrilateralGameObject implements IRenderedObject {

	private Color colour = new Color(0, 0, 0);

	protected Texture texture;

	protected int x;
	protected int y;

	protected int width;
	protected int height;

	protected int scaledX;
	protected int scaledY;

	protected int scaledWidth;
	protected int scaledHeight;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

	public RenderableQuadrilateralGameObject(int x, int y, int width, int height, Texture texture) {
		this.texture = texture;

		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;
	}

	public RenderableQuadrilateralGameObject(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;

		this.width = width;
		this.height = height;
	}

	@Override
	public void render() {
		resize();

		GL11.glColor3f((float) colour.getRed() / 256, (float) colour.getGreen() / 256, (float) colour.getBlue() / 256);
		if (texture != null) {
			RenderingUtils.RenderQuad(scaledX, scaledY, scaledWidth, scaledHeight, texture);
		} 
		else {
			RenderingUtils.RenderQuad(scaledX, scaledY, scaledWidth, scaledHeight);
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
		scaledX = ((int) Math
				.round((double) x / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)))
				+ DisplayUtils.widthOffset;
		scaledY = ((int) Math
				.round((double) y / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2)))
				+ DisplayUtils.heightOffset;

		scaledWidth = (int) Math
				.round((double) width / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
		scaledHeight = (int) Math
				.round((double) height / 1080 * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2));

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}

	@Override
	public boolean isTouching(IRenderedObject object) {
		if (object instanceof RenderableQuadrilateralGameObject) {
			RenderableQuadrilateralGameObject casted = (RenderableQuadrilateralGameObject) object;

			int aX1 = this.x;
			int aX2 = this.x + this.width;

			int aY1 = this.y;
			int aY2 = this.y + this.height;

			int bX1 = casted.x;
			int bX2 = casted.x + casted.width;

			int bY1 = casted.y;
			int bY2 = casted.y + casted.height;

			if (aX1 <= bX2 && aX2 >= bX1 && aY1 <= bY2 && aY2 >= bY1)
				return true;
			else
				return false;
		}
		else if (object instanceof RenderableCircularGameObject) {
			RenderableCircularGameObject casted = (RenderableCircularGameObject) object;

			int[] xQuads = { 
				(this.x), 
				(this.x + this.width) 
			};

			int[] yQuads = {
				(this.y),
				(this.y + this.height)
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
		else {
			return false;
		}
	}

	@Override
	public boolean doesContain(int x, int y) {

		int x1 = this.x;
		int x2 = this.x + this.width;

		int y1 = this.y;
		int y2 = this.y + this.height;

		if ((x > x1 && x < x2) && (y > y1 && y < y2))
			return true;
		else
			return false;
	}
}
