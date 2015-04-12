package astechzgo.luminescent.rendering;

import java.awt.Rectangle;

import org.newdawn.slick.Color;

import astechzgo.luminescent.utils.DisplayUtils;

public class Room extends RenderableQuadrilateralGameObject {

	public Room() {
		super(
				
				DisplayUtils.SCREEN_WIDTH / 45, 
				(DisplayUtils.SCREEN_HEIGHT - DisplayUtils.HEIGHT_OFFSET * 2) / 25 + DisplayUtils.HEIGHT_OFFSET, 
				DisplayUtils.SCREEN_WIDTH - (DisplayUtils.SCREEN_WIDTH / 45 * 2),
				(DisplayUtils.SCREEN_HEIGHT - DisplayUtils.HEIGHT_OFFSET * 2) - ((DisplayUtils.SCREEN_HEIGHT - DisplayUtils.HEIGHT_OFFSET )/ 25 * 2)
				
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
