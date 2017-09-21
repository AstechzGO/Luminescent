package astechzgo.luminescent.rendering;

import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.text.Font;

public class TextLabelRenderer extends RectangularObjectRenderer {

	private final Font font;
	private String text;
	
	private Font.CharRenderer[] chars;
	
	public TextLabelRenderer(WindowCoordinates coordinates, String text) {
		this(coordinates, Font.NORMAL_FONT, text);
	}
	
	public TextLabelRenderer(WindowCoordinates coordinates, Font font, String text) {
		super(coordinates, font.getWidth(text), font.getHeight(text));
		
		this.font = font;
		this.text = text;
	}
	
	@Override
	public void upload(List<Supplier<Matrix4f>> matrices) {	
	    chars = font.drawText(text, coordinates, getColour());
	}
	
	public Font getFont() {
		return font;
	}
	
	public String getText() {
		return text;
	}
	
	public String setText(String text) {
	    if(chars == null) {
	        this.text = text;
	        return this.text;
	    }
	    
	    if(this.text.length() - text.length() > 0) {
	        text = new String(text);
	        int diff = this.text.length() - text.length();
	        for(int i = 0; i < diff; i++) {
	            text += " ";
	        }
	    }
	    
	    this.text = text.substring(0, this.text.length());
	    
	    for(int i = 0; i < this.text.length(); i++) {
	        chars[i].setCharacter(this.text.charAt(i));
	    }
	    
	    return this.text;
	}
}
