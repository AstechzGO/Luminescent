package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class RoomWalls extends RenderableMultipleRenderedObjects {
	
	private static int posX = 1920 / 45;
	private static int posY = 1080 / 25;
	
	private static int width = 1920 - (1920 / 45 * 2);
	private static int height = 1080 - (1080 / 25 * 2);

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
		RenderableQuadrilateralGameObject wall1 = new RenderableRectangularGameObject(
				0,
				0,
				posX,
				1080
			);
		wall1.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall2 = new RenderableRectangularGameObject(
				0,
				0,
				1920,
				posY
			);
		wall2.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall3 = new RenderableRectangularGameObject(
				width + posX,
				0,
				1920 - (width + posX),
				1080
			);
		wall3.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall4 = new RenderableRectangularGameObject(
				0,
				height + posY,
				1920,
				1080 - (height + posY)
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
