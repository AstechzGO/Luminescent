package astechzgo.luminescent.entity;

import java.awt.Color;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.IObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;

public class Projectile extends LivingEntity {
	
	private static final double speed = 2.5;
	
	// Used to get Direction for Shooting
	private double lastDelta;
	private final double rotation;
	
	private final double width;
	private final double height;
	
	private final RectangularObjectRenderer renderer;

	// The Class
	// Constructor for projectile
	public Projectile(GameCoordinates coordinates) {
		renderer = new RectangularObjectRenderer(new WindowCoordinates(coordinates), 5, 5);
		renderer.setColour(new Color(0.5f, 0.6f, 0.2f));
		
		lastDelta = GLFW.glfwGetTime() * 1000;
		rotation = Luminescent.thePlayer.setRotation();
		
		this.width = 5;
		this.height = 5;
		
		this.coordinates = new GameCoordinates(coordinates.getGameCoordinatesX() + (22.5 + width / 2) * Math.cos(Math.toRadians(270 - rotation)), coordinates.getGameCoordinatesZ() + (22.5 + height / 2)  * Math.sin(Math.toRadians(270 - rotation)));
	}

	// Called every tick from Luminescent class, shoots bullet in direction
	public void fireBullet(List<Double> verticalEdges, List<Double> horizontalEdges) {
		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		
		lastDelta = GLFW.glfwGetTime() * 1000;
		
		double speed = Projectile.speed * delta;
		
		double x = 0;
		double z = 0;
		
		boolean bx = false;
		boolean bz = false;
		
		boolean initX = true;
		boolean initZ = true;
		
		this.setAlive(true);
		
		for(double verticalEdge : verticalEdges) {
			double projectedX = getCoordinates().getGameCoordinatesX() + speed * Math.cos(Math.toRadians(rotation - 270));
			
			if(!(getCoordinates().getGameCoordinatesX() > verticalEdge - width) && (projectedX >= verticalEdge - width)) {
				bx = true;
				
				double temp = verticalEdge - width;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}
			else if(!(getCoordinates().getGameCoordinatesX() < verticalEdge) && (projectedX <= verticalEdge)) {
				bx = true;
				
				double temp = verticalEdge;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}					
		}
		
		if(bx) {
			double tempz = getCoordinates().getGameCoordinatesZ();
			coordinates = new GameCoordinates(x, tempz);
			this.setAlive(false);
		}
		else {
			double tempz = getCoordinates().getGameCoordinatesZ();
			coordinates = new GameCoordinates(getCoordinates().getGameCoordinatesX() + speed * Math.cos(Math.toRadians(rotation - 270)), tempz);
		}
		
		for(double horizontalEdge : horizontalEdges) {
			double projectedZ = this.getCoordinates().getGameCoordinatesZ() - speed * Math.sin(Math.toRadians(rotation - 270));
			
			if(!(getCoordinates().getGameCoordinatesZ() > horizontalEdge - height) && (projectedZ >= horizontalEdge - height)) {
				bz = true;
				
				double temp = horizontalEdge - height;
				if(temp > z || initZ) {
					z = temp;
					initZ = false;
				}
			}
			else if(!(getCoordinates().getGameCoordinatesZ() < horizontalEdge) && (projectedZ <= horizontalEdge)) {
				bz = true;
				
				double temp = horizontalEdge;
				if(temp > z || initZ) {
					z = temp;
					initZ = false;
				}
			}					
		}
		
		if(bz) {
			double tempx = getCoordinates().getGameCoordinatesX();
			coordinates = new GameCoordinates(tempx, z);
			this.setAlive(false);
		}
		else {
			double tempx = getCoordinates().getGameCoordinatesX();
			coordinates = new GameCoordinates(tempx, getCoordinates().getGameCoordinatesZ() - speed * Math.sin(Math.toRadians(rotation - 270)));	
		}
	}

	@Override
	public IObjectRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void updateRenderer() {
		renderer.setCoordinates(new WindowCoordinates(coordinates));
	}
}
