package astechzgo.luminescent.rendering;

import astechzgo.luminescent.text.Font;
import astechzgo.luminescent.utils.DisplayUtils;

public class TextLabelRenderer extends RectangularObjectRenderer {

	private Font font;
	private String text;
	
	private Font scaledFont;
	
	private int oldFontSize;
	
	public TextLabelRenderer(double x, double y, String text) {
		this(x, y, Font.NORMAL_FONT, text);
	}
	
	public TextLabelRenderer(double x, double y, Font font, String text) {
		super(x, y, font.getWidth(text), font.getHeight(text));
		
		this.font = font;
		this.text = text;
	}
	
	@Override
	public void resize() {
		super.resize();
		
		int size = (int) Math
				.round(font.getFontSize() / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
		
		if(oldFontSize != size) {
			oldFontSize = size;
			scaledFont = new Font(new java.awt.Font(font.getFontName(), font.getFontStyle(), size));
		}
	}
	
	@Override
	public void render() {
		super.aX = x;
		super.aY = y + height;
		super.bX = x + width;
		super.bY = y + height;
		super.cX = x + width;
		super.cY = y;
		super.dX = x;
		super.dY = y;
		
		resize();
		
		scaledFont.drawText(text, scaledDX, scaledDY, getColour());
	}
	
	public Font getFont() {
		return font;
	}
	
	public void setFont(Font font) {
		this.font = font;
		
		width = font.getWidth(text);
		height = font.getHeight(text);
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		
		width = font.getWidth(text);
		height = font.getHeight(text);
	}
}
