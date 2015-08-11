package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.Rectangle;

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
	
	public Rectangle getBox() {
		return new Rectangle(super.x, super.y, super.width, super.height);
	}
}
