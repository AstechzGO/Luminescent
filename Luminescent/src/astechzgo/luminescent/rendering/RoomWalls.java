package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import astechzgo.luminescent.utils.DisplayUtils;

public class RoomWalls extends RenderableMultipleRenderedObjects {
	
	private static int posX = (DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2) / 45 + DisplayUtils.widthOffset;
	private static int posY = (DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2) / 25 + DisplayUtils.heightOffset;
	
	private static int width = (DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2) - ((DisplayUtils.monitorWidth - DisplayUtils.widthOffset ) / 45 * 2);
	private static int height = (DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2) - ((DisplayUtils.monitorHeight - DisplayUtils.heightOffset )/ 25 * 2);

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
				DisplayUtils.widthOffset,
				DisplayUtils.heightOffset,
				posX,
				DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2
			);
		wall1.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall2 = new RenderableQuadrilateralGameObject(
				DisplayUtils.widthOffset,
				DisplayUtils.heightOffset,
				DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2,
				posY - DisplayUtils.heightOffset
			);
		wall2.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall3 = new RenderableQuadrilateralGameObject(
				width + posX,
				DisplayUtils.heightOffset,
				DisplayUtils.monitorWidth - DisplayUtils.widthOffset - (width + posX),
				DisplayUtils.monitorHeight - DisplayUtils.heightOffset * 2
			);
		wall3.setColour(new Color(0.0f, 0.4f, 0.6f));
		
		RenderableQuadrilateralGameObject wall4 = new RenderableQuadrilateralGameObject(
				DisplayUtils.widthOffset,
				height + posY,
				DisplayUtils.monitorWidth - DisplayUtils.widthOffset * 2,
				DisplayUtils.monitorHeight - DisplayUtils.heightOffset - (height + posY)
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
