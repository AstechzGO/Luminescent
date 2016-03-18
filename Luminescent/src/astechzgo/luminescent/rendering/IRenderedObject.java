package astechzgo.luminescent.rendering;

import java.awt.Color;

import astechzgo.luminescent.textures.Texture;


public interface IRenderedObject
{
	public void render();
	
	public void setColour(Color colour);
	public Color getColour();
	
	public void resize();
	
	public Texture getTexture();
	public void setTexture(Texture texture);
	
	public boolean isTouching(IRenderedObject object);
	public boolean doesContain(double x, double y);
}