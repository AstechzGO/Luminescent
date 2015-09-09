package astechzgo.luminescent.rendering;

import java.awt.Color;

public class Room extends RenderableQuadrilateralGameObject {

	public Room() {
		super(
				
				1920 / 45, 
				1080 / 25, 
				1920 - (1920 / 45 * 2),
				1080 - (1080 / 25 * 2)
				
			);
		super.setColour(new Color(0.15f, 0.15f, 0.15f));
	}
	
	public void setSize(int width, int height) {
		super.width = width;
		super.height = height;
	}

	@Override
	public void render() {
		super.render();
	}
	
	public double getPosX() {
		return super.x;
	}
	
	public double getPosY() {
		return super.y;
	}
	
	public double getWidth() {
		return super.width;
	}
	
	public double getHeight() {
		return super.height;
	}
}
