package astechzgo.luminescent.rendering;

import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.RenderingUtils;

public class RenderableQuadrilateralGameObject implements IRenderedObject {

	protected Texture texture;
	
	protected int x;
	protected int y;
	
	protected int width;
	protected int height;
	
	public RenderableQuadrilateralGameObject(int x, int y, Texture texture) {
		this.texture = texture;
		
		new RenderableQuadrilateralGameObject(x, y, texture.getAsSlickTexture().getImageWidth(), texture.getAsSlickTexture().getImageHeight());
	}
	
	public RenderableQuadrilateralGameObject(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void render() {
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
}
