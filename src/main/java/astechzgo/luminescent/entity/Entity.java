package astechzgo.luminescent.entity;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.rendering.IObjectRenderer;

public interface Entity {

	default public void upload() {
		getRenderer().upload();
	}
	
	default public void queue() {
		getRenderer().queue();
	}
	
	public IObjectRenderer getRenderer();
	
	public void updateRenderer();
	
	public GameCoordinates getCoordinates();
}
