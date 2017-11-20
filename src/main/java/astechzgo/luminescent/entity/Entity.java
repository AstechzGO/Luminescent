package astechzgo.luminescent.entity;

import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.rendering.IObjectRenderer;

public interface Entity {

    default public void upload(List<Supplier<Matrix4f>> matrices) {
        getRenderer().upload(matrices);
    }
    
	default public void upload() {
		getRenderer().upload();
	}
	
	public IObjectRenderer getRenderer();
	
	public void updateRenderer();
	
	public GameCoordinates getCoordinates();
}
