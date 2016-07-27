package astechzgo.luminescent.coordinates;

public class AbsoluteCoordinates {
	
	private double absoluteX;
	private double absoluteY;
	
	protected AbsoluteCoordinates converted;
	
	public AbsoluteCoordinates(double x, double y) {
		this.absoluteX = x;
		this.absoluteY = y;
	}

	public AbsoluteCoordinates(AbsoluteCoordinates coords) {
		converted = coords.getOriginalCoordinates();
	}
	
	public double getAbsoluteX() {
		if(converted != null)
			return converted.getAbsoluteX();
		else
			return absoluteX;
	}
	
	public double getAbsoluteY() {
		if(converted != null)
			return converted.getAbsoluteY();
		else
			return absoluteY;
	}
	
	protected AbsoluteCoordinates getOriginalCoordinates() {
		AbsoluteCoordinates origin = this;
		while(origin.converted != null) {
			origin = origin.converted;
		}
		
		return origin;
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AbsoluteCoordinates && getAbsoluteX() == ((AbsoluteCoordinates)obj).getAbsoluteX() && getAbsoluteY() == ((AbsoluteCoordinates)obj).getAbsoluteY();
	}
}
