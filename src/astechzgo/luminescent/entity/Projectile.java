package astechzgo.luminescent.entity;

import java.awt.Color;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.IObjectRenderer;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;

public class Projectile extends LivingEntity {
	
	private static final double speed = 2.5;
	
	// Used to get Direction for Shooting
	private double lastDelta;
	private final double rotation;
	
	private double x;
	private double y;
	
	private final double width;
	private final double height;
	
	private final RectangularObjectRenderer renderer;

	// The Class
	// Constructor for projectile
	public Projectile(double x, double y) {
		renderer = new RectangularObjectRenderer(x, y, 5, 5);
		renderer.setColour(new Color(0.5f, 0.6f, 0.2f));
		
		lastDelta = GLFW.glfwGetTime() * 1000;
		rotation = Luminescent.thePlayer.setRotation();
		
		this.width = 5;
		this.height = 5;
		
		this.x = (int) (x + (22.5 + width / 2) * Math.cos(Math.toRadians(270 - rotation)));
		this.y = (int) (y + (22.5 + height / 2)  * Math.sin(Math.toRadians(270 - rotation)));
	}

	// Called every tick from Luminescent class, shoots bullet in direction
	public void fireBullet(List<Double> verticalEdges, List<Double> horizontalEdges) {
		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		
		lastDelta = GLFW.glfwGetTime() * 1000;
		
		double speed = Projectile.speed * delta;
		
		double x = 0;
		double y = 0;
		
		boolean bx = false;
		boolean by = false;
		
		boolean initX = true;
		boolean initY = true;
		
		this.setAlive(true);
		
		for(double verticalEdge : verticalEdges) {
			double projectedX = this.x + speed * Math.cos(Math.toRadians(rotation - 270));
			
			if(!(this.x > verticalEdge - width) && (projectedX >= verticalEdge - width)) {
				bx = true;
				
				double temp = verticalEdge - width;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}
			else if(!(this.x < verticalEdge) && (projectedX <= verticalEdge)) {
				bx = true;
				
				double temp = verticalEdge;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}					
		}
		
		if(bx) {
			this.x = x;
			this.setAlive(false);
		}
		else
			this.x = this.x + speed * Math.cos(Math.toRadians(rotation - 270));
		
		for(double horizontalEdge : horizontalEdges) {
			double projectedY = this.y - speed * Math.sin(Math.toRadians(rotation - 270));
			
			if(!(this.y > horizontalEdge - height) && (projectedY >= horizontalEdge - height)) {
				by = true;
				
				double temp = horizontalEdge - height;
				if(temp > y || initY) {
					y = temp;
					initY = false;
				}
			}
			else if(!(this.y < horizontalEdge) && (projectedY <= horizontalEdge)) {
				by = true;
				
				double temp = horizontalEdge;
				if(temp > y || initY) {
					y = temp;
					initY = false;
				}
			}					
		}
		
		if(by) {
			this.y = y;
			this.setAlive(false);
		}
		else
			this.y = this.y - speed * Math.sin(Math.toRadians(rotation - 270));		
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	@Override
	public IObjectRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void updateRenderer() {
		renderer.setX(x);
		renderer.setY(y);
	}
}
