package astechzgo.luminescent.coordinates;

import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.utils.DisplayUtils;

public class GameCoordinates extends AbsoluteCoordinates {

	private double gameCoordinatesX;
	private double gameCoordinatesZ;
	
	public GameCoordinates(double x, double z) {
		super(0, 0);
		
		this.gameCoordinatesX = x;
		this.gameCoordinatesZ = z;
	}
	
	public GameCoordinates(AbsoluteCoordinates coords) {
		super(coords);
		
		if(converted instanceof GameCoordinates) {
			gameCoordinatesX = ((GameCoordinates)converted).gameCoordinatesX;
			gameCoordinatesZ = ((GameCoordinates)converted).gameCoordinatesZ;
			
			converted = null;
		}
	}
	
	public double getAbsoluteX() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return(gameCoordinatesX + (Camera.CAMERA_WIDTH / 2) - Camera.getCameraCoordinates().getGameCoordinatesX()) / Camera.CAMERA_WIDTH  * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)  + DisplayUtils.getDisplayX();
	}
	
	public double getAbsoluteY() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return (gameCoordinatesZ + (Camera.CAMERA_HEIGHT / 2) - Camera.getCameraCoordinates().getGameCoordinatesZ()) / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2)  + DisplayUtils.getDisplayY();
	}
	
	public double getGameCoordinatesX() {
		if(converted != null)
			return (converted.getAbsoluteX() - DisplayUtils.getDisplayX()) / (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2) * Camera.CAMERA_WIDTH + Camera.getCameraCoordinates().getGameCoordinatesX() - (Camera.CAMERA_WIDTH / 2);
		else
			return gameCoordinatesX;
	}
	
	public double getGameCoordinatesZ() {
		if(converted != null)
			return (converted.getAbsoluteY() - DisplayUtils.getDisplayY()) / (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2) * Camera.CAMERA_HEIGHT + Camera.getCameraCoordinates().getGameCoordinatesZ() - (Camera.CAMERA_HEIGHT / 2);
		else
			return gameCoordinatesZ;
	}
}
