package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.textures.Texture;


public interface IObjectRenderer
{
    default void upload() {
	    upload(this::getModelMatrix);
	}
	
    default void upload(Supplier<Matrix4f> matrix) {
        upload(List.of(matrix));
    }
    
    void upload(List<Supplier<Matrix4f>> matrices);
	
	void setColour(Color colour);
	Color getColour();
	
	void resize();
	
	Texture getTexture();
	void setTexture(Texture texture);
	
	boolean isTouching(IObjectRenderer object);
	boolean doesContain(double x, double y);
	
	WindowCoordinates getCoordinates();
	
	void setCoordinates(WindowCoordinates coordinates);
	
	Matrix4f getModelMatrix();

	void setDoesLighting(boolean doLighting);
	boolean doesLighting();
}