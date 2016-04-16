package astechzgo.luminescent.entity;

import astechzgo.luminescent.rendering.RenderableQuadrilateralGameObject;
import astechzgo.luminescent.textures.Texture;

public class QuadrilateralEntity extends RenderableQuadrilateralGameObject implements Entity {

	public QuadrilateralEntity(double aX, double aY, double bX, double bY, double cX, double cY, double dX, double dY) {
		super(aX, aY, bX, bY, cX, cY, dX, dY);
		// TODO Auto-generated constructor stub
	}

	public QuadrilateralEntity(double aX, double aY, double bX, double bY, double cX, double cY, double dX, double dY, Texture texture) {
		super(aX, aY, bX, bY, cX, cY, dX, dY, texture);
		// TODO Auto-generated constructor stub
	}
}
