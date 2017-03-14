package astechzgo.luminescent.coordinates;

import astechzgo.luminescent.utils.DisplayUtils;

public class ScaledWindowCoordinates extends AbsoluteCoordinates {
	
	private double scaledWindowCoordinatesX;
	private double scaledWindowCoordinatesY;
	
	public ScaledWindowCoordinates(double x, double y) {
		super(0, 0);
		
		this.scaledWindowCoordinatesX = x;
		this.scaledWindowCoordinatesY = y;
	}
	
	public ScaledWindowCoordinates(AbsoluteCoordinates coords) {
		super(coords);
		
		if(converted instanceof ScaledWindowCoordinates) {
			scaledWindowCoordinatesX = ((ScaledWindowCoordinates)converted).scaledWindowCoordinatesX;
			scaledWindowCoordinatesY = ((ScaledWindowCoordinates)converted).scaledWindowCoordinatesY;
				
			converted = null;
		}
	}
	
	public double getAbsoluteX() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return scaledWindowCoordinatesX + DisplayUtils.widthOffset + DisplayUtils.getDisplayX();
	}
	
	public double getAbsoluteY() {
		if(converted != null)
			return converted.getAbsoluteY();
		else
			return scaledWindowCoordinatesY + DisplayUtils.heightOffset + DisplayUtils.getDisplayY();
	}
	
	public double getScaledWindowCoordinatesX() {
		if(converted != null)
			return converted.getAbsoluteX() - DisplayUtils.getDisplayX() - DisplayUtils.widthOffset;
		else
			return scaledWindowCoordinatesX;
	}
	
	public double getScaledWindowCoordinatesY() {
		if(converted != null)
			return converted.getAbsoluteY() - DisplayUtils.getDisplayY() - DisplayUtils.heightOffset;
		else
			return scaledWindowCoordinatesY;
	}
}
