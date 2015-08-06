package astechzgo.luminescent.rendering;

import java.awt.Color;


public interface IRenderedObject
{
	public void render();
	
	public void setColour(Color colour);
	public Color getColour();
}