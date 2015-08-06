package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.Rectangle;

import astechzgo.luminescent.utils.DisplayUtils;

public class Room extends RenderableQuadrilateralGameObject {

	public Room() {
		super(
				
				(DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2) / 45 + DisplayUtils.widthOffset, 
				(DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2) / 25 + DisplayUtils.heightOffset, 
				(DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2) - ((DisplayUtils.monitorWidth - DisplayUtils.widthOffset ) / 45 * 2),
				(DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2) - ((DisplayUtils.monitorHeight - DisplayUtils.heightOffset )/ 25 * 2)
				
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
