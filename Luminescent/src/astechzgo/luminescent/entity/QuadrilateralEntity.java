package astechzgo.luminescent.entity;

import astechzgo.luminescent.rendering.RenderableQuadrilateralGameObject;
import astechzgo.luminescent.textures.Texture;

public class QuadrilateralEntity extends RenderableQuadrilateralGameObject implements Entity {

	public QuadrilateralEntity(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY) {
		super(aX, aY, bX, bY, cX, cY, dX, dY);
		// TODO Auto-generated constructor stub
	}

	public QuadrilateralEntity(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY, Texture texture) {
		super(aX, aY, bX, bY, cX, cY, dX, dY, texture);
		// TODO Auto-generated constructor stub
	}
}
