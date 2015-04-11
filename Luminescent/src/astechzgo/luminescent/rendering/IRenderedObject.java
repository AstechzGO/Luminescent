package astechzgo.luminescent.rendering;

import org.newdawn.slick.Color;

public interface IRenderedObject
{
	public void render();
	
	public void setColour(Color colour);
	public Color getColour();
}