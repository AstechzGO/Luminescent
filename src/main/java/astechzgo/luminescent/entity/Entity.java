package astechzgo.luminescent.entity;

import java.util.List;
import java.util.function.Supplier;

import org.joml.Matrix4f;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.rendering.IObjectRenderer;

public interface Entity {

    default void upload(List<Supplier<Matrix4f>> matrices) {
        getRenderer().upload(matrices);
    }
    
	default void upload() {
		getRenderer().upload();
	}
	
	IObjectRenderer getRenderer();
	
	void updateRenderer();
	
	GameCoordinates getCoordinates();
}
