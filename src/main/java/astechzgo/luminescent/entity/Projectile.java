package astechzgo.luminescent.entity;

import java.awt.Color;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.IObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.DisplayUtils;

public class Projectile extends LivingEntity {
	
	private static final double speed = 2.5;
	
	// Used to get Direction for Shooting
	private double lastDelta = -1;
	private double rotation;
	
	private final double width;
	private final double height;
	
	private final RectangularObjectRenderer renderer;

	// The Class
	// Constructor for projectile
	public Projectile(GameCoordinates coordinates) {
		renderer = new ProjectileRenderer(new WindowCoordinates(coordinates), 5, 5);
		renderer.setColour(new Color(0.5f, 0.6f, 0.2f));
		
		
		
		this.width = 5;
		this.height = 5;
	}

	// Called every tick from Luminescent class, shoots bullet in direction
	public void fireBullet(List<Double> verticalEdges, List<Double> horizontalEdges) {
	    if(lastDelta == -1) {
	        lastDelta = GLFW.glfwGetTime() * 1000;
	    }
	    
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

				if(verticalEdge > x || initX) {
					x = verticalEdge;
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
			double projectedZ = this.getCoordinates().getGameCoordinatesZ() + speed * Math.sin(Math.toRadians(rotation - 270));
			
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

				if(horizontalEdge > z || initZ) {
					z = horizontalEdge;
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
			coordinates = new GameCoordinates(tempx, getCoordinates().getGameCoordinatesZ() + speed * Math.sin(Math.toRadians(rotation - 270)));	
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
	
	private class ProjectileRenderer extends RectangularObjectRenderer {

        public ProjectileRenderer(WindowCoordinates coordinates, double width, double height) {
            super(coordinates, width, height);
        }
        
        public ProjectileRenderer(WindowCoordinates coordinates, double width, double height, Texture texture) {
            super(coordinates, width, height, texture);
        }
        
        @Override
        public void resize() {
            super.resize();
            
            oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
            oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
            
            ScaledWindowCoordinates loc = new ScaledWindowCoordinates(this.getCoordinates());
            Vector3f location = new Vector3f((float)loc.getScaledWindowCoordinatesX() + DisplayUtils.widthOffset, (float)loc.getScaledWindowCoordinatesY()  + DisplayUtils.heightOffset, 0.0f);

			this.model = new Matrix4f().translation(location).scale(isAlive ? (float) (1.0 / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)) : 0);
        }
	}
	
	public void init(GameCoordinates coordinates) {
	    rotation = Luminescent.thePlayer.setRotation();
	    setAlive(true);
	    
	    this.coordinates = new GameCoordinates(coordinates.getGameCoordinatesX() + (22.5 + width / 2) * Math.cos(Math.toRadians(rotation - 270)), coordinates.getGameCoordinatesZ() + (22.5 + height / 2)  * Math.sin(Math.toRadians(rotation - 270)));
	}
}
