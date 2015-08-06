package astechzgo.luminescent.rendering;



import java.awt.Color;

import org.lwjgl.opengl.GL11;

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
		
		this.width = texture.getAsBufferedImage().getWidth();
		this.height = texture.getAsBufferedImage().getHeight();
	}
	
	public RenderableQuadrilateralGameObject(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render() {
		GL11.glColor3f((float)colour.getRed() / 256, (float)colour.getGreen() / 256, (float)colour.getBlue() / 256);
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
