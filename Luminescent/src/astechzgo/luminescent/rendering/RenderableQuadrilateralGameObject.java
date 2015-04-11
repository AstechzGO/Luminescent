package astechzgo.luminescent.rendering;



import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;

import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.RenderingUtils;

public class RenderableQuadrilateralGameObject implements IRenderedObject {

	private Color colour = new Color(0, 0, 0);
	
	protected Texture texture;
	
	protected int x;
	protected int y;
	
	protected int width;
	protected int height;
	
	public RenderableQuadrilateralGameObject(int x, int y, Texture texture) {
		this.texture = texture;
		
		this.x = x;
		this.y = y;
		
		this.width = texture.getAsSlickTexture().getImageWidth();
		this.height = texture.getAsSlickTexture().getImageHeight();
	}
	
	public RenderableQuadrilateralGameObject(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render() {
		GL11.glColor3f(colour.r, colour.g, colour.b);
		if(texture != null) {
			RenderingUtils.RenderQuad(x, y, width, height, texture);
		}
		else {
			RenderingUtils.RenderQuad(x, y, width, height);
		}
	}

	public void setTexture(Texture texture) {
		this.texture = texture;
	}

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
}
