package astechzgo.luminescent.coordinates;

import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.utils.DisplayUtils;

public class WindowCoordinates extends AbsoluteCoordinates {

	private double windowCoordinatesX;
	private double windowCoordinatesY;
	
	public WindowCoordinates(double x, double y) {
		super(0, 0);
		
		this.windowCoordinatesX = x;
		this.windowCoordinatesY = y;
	}
	
	public WindowCoordinates(AbsoluteCoordinates coords) {
		super(coords);
		
		if(converted instanceof WindowCoordinates) {
			windowCoordinatesX = ((WindowCoordinates)converted).windowCoordinatesX;
			windowCoordinatesY = ((WindowCoordinates)converted).windowCoordinatesY;
			
			converted = null;
		}
	}
	
	public double getAbsoluteX() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return (windowCoordinatesX + DisplayUtils.widthOffset) / Camera.CAMERA_WIDTH  * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)  + DisplayUtils.getDisplayX();
	}
	
	public double getAbsoluteY() {
		if(converted != null)
			return converted.getAbsoluteY();
		else
			return windowCoordinatesY / Camera.CAMERA_HEIGHT * (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2) + DisplayUtils.getDisplayY();
	}
	
	public double getWindowCoordinatesX() {
		if(converted != null)
			return (converted.getAbsoluteX() - DisplayUtils.getDisplayX()) / (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2) * Camera.CAMERA_WIDTH;
		else
			return windowCoordinatesX;
	}
	
	public double getWindowCoordinatesY() {
		if(converted != null)
			return (converted.getAbsoluteY() - DisplayUtils.getDisplayY()) / (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2) * Camera.CAMERA_HEIGHT;
		else
			return windowCoordinatesY;
	}
}
