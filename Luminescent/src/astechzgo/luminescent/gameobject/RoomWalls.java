package astechzgo.luminescent.gameobject;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.IRenderedObject;
import astechzgo.luminescent.rendering.RenderableMultipleRenderedObjects;
import astechzgo.luminescent.rendering.RenderableQuadrilateralGameObject;
import astechzgo.luminescent.rendering.RenderableRectangularGameObject;

public class RoomWalls extends RenderableMultipleRenderedObjects {
	
	private static int posX = Camera.CAMERA_WIDTH / 45;
	private static int posY = Camera.CAMERA_HEIGHT / 25;
	
	private static int width = Camera.CAMERA_WIDTH - (Camera.CAMERA_WIDTH / 45 * 2);
	private static int height = Camera.CAMERA_HEIGHT - (Camera.CAMERA_HEIGHT / 25 * 2);

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
				Camera.CAMERA_HEIGHT
			);
		wall1.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall2 = new RenderableRectangularGameObject(
				0,
				0,
				Camera.CAMERA_WIDTH,
				posY
			);
		wall2.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall3 = new RenderableRectangularGameObject(
				width + posX,
				0,
				Camera.CAMERA_WIDTH - (width + posX),
				Camera.CAMERA_HEIGHT
			);
		wall3.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall4 = new RenderableRectangularGameObject(
				0,
				height + posY,
				Camera.CAMERA_WIDTH,
				Camera.CAMERA_HEIGHT - (height + posY)
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
