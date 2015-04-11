package astechzgo.luminescent.rendering;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;

import astechzgo.luminescent.utils.DisplayUtils;

public class RoomWalls extends RenderableMultipleRenderedObjects {
	
	private static int posX = DisplayUtils.SCREEN_WIDTH / 45;
	private static int posY = DisplayUtils.SCREEN_HEIGHT / 25;
	
	private static int width = DisplayUtils.SCREEN_WIDTH - (DisplayUtils.SCREEN_WIDTH / 45 * 2);
	private static int height = DisplayUtils.SCREEN_HEIGHT - (DisplayUtils.SCREEN_HEIGHT / 25 * 2);

	public RoomWalls() {
		 super(getRoomObjects());
		 super.setColour(new Color(0.0f, 0.4f, 0.6f));
	}
	
	public void setSize(int width, int height) {
		RoomWalls.width = width;
		RoomWalls.height = height;
	}

	@Override
	public void render() {
		super.render();
	}
	
	private static List<IRenderedObject> getRoomObjects() {
		RenderableQuadrilateralGameObject wall1 = new RenderableQuadrilateralGameObject(
				0,
				0,
				posX,
				DisplayUtils.SCREEN_HEIGHT
			);
		wall1.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall2 = new RenderableQuadrilateralGameObject(
				0,
				0,
				DisplayUtils.SCREEN_WIDTH,
				posY
			);
		wall2.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall3 = new RenderableQuadrilateralGameObject(
				width + posX,
				0,
				DisplayUtils.SCREEN_WIDTH - (width + posX),
				DisplayUtils.SCREEN_HEIGHT
			);
		wall3.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall4 = new RenderableQuadrilateralGameObject(
				0,
				height + posY,
				DisplayUtils.SCREEN_WIDTH,
				DisplayUtils.SCREEN_HEIGHT - (height + posY)
			);
		wall4.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		List<IRenderedObject> roomObjects = new ArrayList<IRenderedObject>();
		IRenderedObject[] objectArray = {wall1, wall2, wall3, wall4};
		
		 for(IRenderedObject roomObject :  objectArray) {
			 roomObjects.add(roomObject);
		 }
		 
		 return roomObjects;
	}
}
