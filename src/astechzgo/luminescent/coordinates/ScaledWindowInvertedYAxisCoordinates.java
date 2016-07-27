package astechzgo.luminescent.coordinates;

import astechzgo.luminescent.utils.DisplayUtils;

public class ScaledWindowInvertedYAxisCoordinates extends AbsoluteCoordinates {
	
	private double scaledWindowInvertedYAxisCoordinatesX;
	private double scaledWindowInvertedYAxisCoordinatesY;
	
	public ScaledWindowInvertedYAxisCoordinates(double x, double y) {
		super(0, 0);
		
		this.scaledWindowInvertedYAxisCoordinatesX = x;
		this.scaledWindowInvertedYAxisCoordinatesY = y;
	}
	
	public ScaledWindowInvertedYAxisCoordinates(AbsoluteCoordinates coords) {
		super(coords);
		
		if(converted instanceof ScaledWindowInvertedYAxisCoordinates) {
			scaledWindowInvertedYAxisCoordinatesX = ((ScaledWindowInvertedYAxisCoordinates)converted).scaledWindowInvertedYAxisCoordinatesX;
			scaledWindowInvertedYAxisCoordinatesY = ((ScaledWindowInvertedYAxisCoordinates)converted).scaledWindowInvertedYAxisCoordinatesY;
			
			converted = null;
		}
	}
	
	public double getAbsoluteX() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return scaledWindowInvertedYAxisCoordinatesX + DisplayUtils.getDisplayX();
	}
	
	public double getAbsoluteY() {
		if(converted != null)
			return converted.getAbsoluteY();
		else
			return DisplayUtils.getDisplayHeight() - scaledWindowInvertedYAxisCoordinatesY + DisplayUtils.getDisplayY();
	}
	
	public double getScaledWindowCoordinatesX() {
		if(converted != null)
			return converted.getAbsoluteX() - DisplayUtils.getDisplayX();
		return scaledWindowInvertedYAxisCoordinatesX;
	}
	
	public double getScaledWindowCoordinatesY() {
		if(converted != null)
			return  -(converted.getAbsoluteY() - DisplayUtils.getDisplayY() - DisplayUtils.getDisplayHeight());
		return scaledWindowInvertedYAxisCoordinatesY;
	}
}
