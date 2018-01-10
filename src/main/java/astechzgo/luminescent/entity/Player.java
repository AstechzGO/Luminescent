package astechzgo.luminescent.entity;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.keypress.KeyPressGameplay;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.CircularObjectRenderer;
import astechzgo.luminescent.rendering.IObjectRenderer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public abstract class Player extends LivingEntity {

	private double radius;
	private double rotation;
	
	private final CircularObjectRenderer renderer;
	
	public static final double slowSpeed = 0.5;
	public static final double fastSpeed = 0.88;

	private double lastDelta;

	protected Player(CircularObjectRenderer renderer) {
		this.renderer = renderer;
		this.lastDelta = GLFW.glfwGetTime() * 1000;
		
		this.radius = 40;
		this.coordinates = new GameCoordinates(renderer.getCoordinates());
	}
	
	@Override
	public GameCoordinates getCoordinates() {
		return coordinates;
	}
	
	public void setCoordinates(GameCoordinates coordinates) {
		super.coordinates = coordinates;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return radius;
	}

	public abstract MovementInfo getMove();

	private double lastShot = 0;

	public void move(List<Room> rooms) {

		MovementInfo move = getMove();
		this.setRotation(move.rotation);

	    updateRenderer();
	    
		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		lastDelta = GLFW.glfwGetTime() * 1000;

		if(move.shooting && lastDelta - lastShot > 5) {
			KeyPressGameplay.shoot(this);
			lastShot = lastDelta;
		}

		double speed = 0;
		
		if(move.fast) {
			speed = Player.fastSpeed * delta;
		}
		else {
			speed = Player.slowSpeed * delta;
		}	
		
		boolean down = true;
		
		double tempAngle = -1;
		
		if(move.up != move.down) {
			down = false;
			
			if(move.up)
				tempAngle = 90;
			else
				tempAngle = 270;
		}
		if(move.left != move.right) {
			down = false;
			
			if(move.left)
				if(tempAngle == -1)
					tempAngle =  180;
				else if(move.up)
					tempAngle = (tempAngle + 360 + 270);
				else
					tempAngle = (tempAngle + 180) / 2;
			else
				if(tempAngle == -1)
					tempAngle =  0;
				else if(move.up)
					tempAngle = tempAngle / 2;
				else
					tempAngle = (tempAngle + 360) / 2;
		}
					
		if(down)
			speed = 0;		
		
		if(tempAngle == -1) tempAngle = 90;
		
		double angle = move.rotation + tempAngle;
		
		List<Double> verticalEdges = getVerticalEdges(rooms);
		List<Double> horizontalEdges = getHorizontalEdges(rooms);
		
		double x = 0;
		double z = 0;
		
		boolean bx = false;
		boolean bz = false;
		
		boolean initX = true;
		boolean initZ = true;
		
		for(double verticalEdge : verticalEdges) {
			double projectedX = this.getCoordinates().getGameCoordinatesX() + speed * Math.cos(Math.toRadians(angle));
			
			if(!(this.getCoordinates().getGameCoordinatesX() > verticalEdge - this.radius) && (projectedX >= verticalEdge - this.radius)) {
				bx = true;
				
				double temp = verticalEdge - this.radius;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}
			else if(!(this.getCoordinates().getGameCoordinatesX() < verticalEdge + this.getRadius()) && (projectedX <= verticalEdge + this.getRadius())) {
				bx = true;
				
				double temp = verticalEdge + this.radius;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}					
		}
		
		if(bx) {
			double tempz = getCoordinates().getGameCoordinatesZ();
			this.setCoordinates(new GameCoordinates(x, tempz));
		}
		else {
			double tempz = getCoordinates().getGameCoordinatesZ();
			this.setCoordinates(new GameCoordinates(getCoordinates().getGameCoordinatesX() + speed * Math.cos(Math.toRadians(angle)), tempz));
		}
		
		/*for(Room room : rooms) {
			if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - this.getRadius()) {
				double temp = room.getPosY() + room.getHeight() - this.getRadius();
				if(temp > y || initY) {
					y = temp;
					initY = false;
				}
			}
			else if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + this.getRadius()) {
				double temp = room.getPosY() + this.getRadius();
				if(temp < y || initY) {
					y = temp;
					initY = false;
				}
			}
			else {
				by = true;
			}
		}*/
		
		for(double horizontalEdge : horizontalEdges) {
			double projectedZ = getCoordinates().getGameCoordinatesZ() + speed * Math.sin(Math.toRadians(angle));
			
			if(!(getCoordinates().getGameCoordinatesZ() > horizontalEdge - this.radius) && (projectedZ >= horizontalEdge - this.radius)) {
				bz = true;
				
				double temp = horizontalEdge - this.radius;
				if(temp > z || initZ) {
					z = temp;
					initZ = false;
				}
			}
			else if(!(getCoordinates().getGameCoordinatesZ() < horizontalEdge + this.getRadius()) && (projectedZ <= horizontalEdge + this.getRadius())) {
				bz = true;
				
				double temp = horizontalEdge + this.radius;
				if(temp > z || initZ) {
					z = temp;
					initZ = false;
				}
			}					
		}
		
		if(bz) {
			double tempx = getCoordinates().getGameCoordinatesX();
			setCoordinates(new GameCoordinates(tempx, z));
		}
		else {
			//this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
			double tempx = getCoordinates().getGameCoordinatesX();
			setCoordinates(new GameCoordinates(tempx, getCoordinates().getGameCoordinatesZ() + speed * Math.sin(Math.toRadians(angle))));
		}
			
		/*if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP)) {

			if((this.getPosY() + speed) >= room.getPosY() + room.getHeight() - this.getRadius())
				this.setPosY(room.getPosY() + room.getHeight() - this.getRadius());	
			else
				this.setPosY(this.getPosY() + speed);
		}
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN)) {
		
			if((this.getPosY() - speed) <= room.getPosY() + this.getRadius())
				this.setPosY(room.getPosY() + this.getRadius());
			else
				this.setPosY(this.getPosY() - speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT)) {
	
			if((this.getPosX() + speed) >= room.getPosX() + room.getWidth() - this.getRadius())
				this.setPosX(room.getPosX() + room.getWidth() - this.getRadius());
			else
				this.setPosX(this.getPosX() + speed);
		}
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT)) {
		
			if((this.getPosX() - speed) <= room.getPosX() + this.getRadius())
				this.setPosX(room.getPosX() + this.getRadius());
			else
				this.setPosX(this.getPosX() - speed);
		}*/	
	}
	
	// TODO: Simplify edges
	private List<Double> getVerticalEdges(List<Room> rooms) {
		List<Double> verticalEdges = new ArrayList<>();
	
		for(Room room : rooms) {
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX());
			verticalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesX() + room.getWidth());
		}
		
		return verticalEdges;
	}
	
	private List<Double> getHorizontalEdges(List<Room> rooms) {
		List<Double> horizontalEdges = new ArrayList<>();
		
		for(Room room : rooms) {
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ() + room.getHeight());
			horizontalEdges.add(new GameCoordinates(room.getCoordinates()).getGameCoordinatesZ());
		}
		
		return horizontalEdges;
	}

	@Override
	public IObjectRenderer getRenderer() {
		return renderer;
	}

	@Override
	public void updateRenderer() {
		renderer.setRotation(rotation);
		renderer.setCoordinates(new WindowCoordinates(coordinates));
	}

	protected double getRotation() {
		return rotation;
	}

	protected void setRotation(double rotation) {
		this.rotation = rotation;
	}

	protected static class MovementInfo {

		public final boolean up;
		public final boolean down;
		public final boolean right;
		public final boolean left;
		public final boolean fast;
		public final double rotation;
		public final boolean shooting;

		public MovementInfo(boolean up, boolean down, boolean right, boolean left, boolean fast, double rotation, boolean shooting) {
			this.up = up;
			this.down = down;
			this.right = right;
			this.left = left;
			this.fast = fast;
			this.rotation = rotation;
			this.shooting = shooting;
		}
	}
}