package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;


public interface IObjectRenderer
{
    @SuppressWarnings("unchecked")
    default public void upload() {
	    upload(this::getModelMatrix);
	}
	
	//TODO: Uncomment when updated to Java 9- @SafeVarargs
    public void upload(@SuppressWarnings("unchecked") Supplier<Matrix4f>... matrices);
	
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