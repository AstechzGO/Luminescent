package astechzgo.luminescent.rendering;

import java.awt.Color;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.textures.Texture;


public interface IObjectRenderer
{
	default public void queue() {
		Luminescent.renderingQueue.add(this);
	}
	
	public void upload();
	
	public void setColour(Color colour);
	public Color getColour();
	
	public void resize();
	
	public Texture getTexture();
	public void setTexture(Texture texture);
	
	public boolean isTouching(IObjectRenderer object);
	public boolean doesContain(double x, double y);
	
	public WindowCoordinates getCoordinates();
	
	public void setCoordinates(WindowCoordinates coordinates);
	
	public Matrix4f getModelMatrix();
}