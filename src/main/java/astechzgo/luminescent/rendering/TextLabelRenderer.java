package astechzgo.luminescent.rendering;

import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.text.Font;

public class TextLabelRenderer extends RectangularObjectRenderer {

	private final Font font;
	private final String text;
	
	public TextLabelRenderer(WindowCoordinates coordinates, String text) {
		this(coordinates, Font.NORMAL_FONT, text);
	}
	
	public TextLabelRenderer(WindowCoordinates coordinates, Font font, String text) {
		super(coordinates, font.getWidth(text), font.getHeight(text));
		
		this.font = font;
		this.text = text;
	}
	
	@Override
	public void upload(@SuppressWarnings("unchecked") Supplier<Matrix4f>... matrices) {	
	    font.drawText(text, coordinates, getColour());
	}
	
	public Font getFont() {
		return font;
	}
	
	public String getText() {
		return text;
	}
}
