package astechzgo.luminescent.rendering;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;

public class RectangularObjectRenderer extends QuadrilateralObjectRenderer {

	protected Texture texture;

	protected WindowCoordinates coordinates;

	protected double width;
	protected double height;

	protected int scaledWidth;
	protected int scaledHeight;

	protected int oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
	protected int oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

	public RectangularObjectRenderer(WindowCoordinates coordinates, double width, double height, Texture texture) {
		super(new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY()), new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY()), texture);
		
		this.texture = texture;

		this.coordinates = coordinates;

		this.width = width;
		this.height = height;
	}

	public RectangularObjectRenderer(WindowCoordinates coordinates, double width, double height) {
		super(new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY()), new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY()));
		
		this.coordinates = coordinates;

		this.width = width;
		this.height = height;
	}
	public void render() {
		super.a = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height);
		super.b = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height);
		super.c = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY());
		super.d = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY());
				
		super.render();
	}
	
	@Override
	public WindowCoordinates getCoordinates() {
		return coordinates;
	}

	@Override
	public void setCoordinates(WindowCoordinates coordinates) {
		this.coordinates = coordinates;
	}
}
