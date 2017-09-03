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
		super(new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY()),  new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY()), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height), texture);
		
		this.texture = texture;

		this.coordinates = coordinates;

		this.width = width;
		this.height = height;
	}

	public RectangularObjectRenderer(WindowCoordinates coordinates, double width, double height) {
        super(new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY()),  new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY()), new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height), new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height));
		
		this.coordinates = coordinates;

		this.width = width;
		this.height = height;
	}
	
	@Override
	public void upload() {
		super.a = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY());
		super.b = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY());
		super.c = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height);
		super.d = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height);
				
		super.upload();
	}
	
	@Override
	public void resize() {
	    super.a = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY());
	    super.b = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY());
	    super.c = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height);
	    super.d = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height);
	
	    super.resize();
	}
	
	@Override
	public void setCoordinates(WindowCoordinates coordinates) {
	    this.coordinates = coordinates;
	}
	
	@Override
	public WindowCoordinates getCoordinates() {
		return coordinates;
	}
}
