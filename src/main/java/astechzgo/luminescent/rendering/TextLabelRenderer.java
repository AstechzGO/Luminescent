package astechzgo.luminescent.rendering;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.text.Font;

public class TextLabelRenderer extends RectangularObjectRenderer {

	private final Font font;
	private final String text;
	
	//private RectangularObjectRenderer[] characters;
	
	public TextLabelRenderer(WindowCoordinates coordinates, String text) {
		this(coordinates, Font.NORMAL_FONT, text);
	}
	
	public TextLabelRenderer(WindowCoordinates coordinates, Font font, String text) {
		super(coordinates, font.getWidth(text), font.getHeight(text));
		
		this.font = font;
		this.text = text;
	}
	
	@Override
	public void upload() {	
	    font.drawText(text, coordinates, getColour());
	}
	
	public Font getFont() {
		return font;
	}
	
	public String getText() {
		return text;
	}
}
