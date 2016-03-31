package astechzgo.luminescent.entity;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;

import static astechzgo.luminescent.keypress.Key.*;

public class Player extends CircularEntity {
	public static final double slowSpeed = 0.5;
	public static final double fastSpeed = 0.88;
	
	private double lastDelta;
	
	private double lastControllerDelta = 0;
	private double lastMouseX = 0;
	private double lastMouseY = 0;
	
	public Player() {
		super(Camera.CAMERA_WIDTH / 2, Camera.CAMERA_HEIGHT / 2, 40, 1);
		lastDelta = GLFW.glfwGetTime() * 1000;
		lastControllerDelta = GLFW.glfwGetTime() * 1000;
	}
	
	public double getPosX() {
		return super.x;
	}
	
	public double getPosY() {
		return super.y;
	}
	
	public void setPosX(double position) {
		super.x = position;
	}
	
	public void setPosY(double position) {
		super.y = position;
	}
	
	public void setRadius(double radius) {
		this.radius = radius;
	}
	
	public double getRadius() {
		return radius;
	}
	
	@Override
	public void resize() {
		scaledX = ((int) Math
				.round((double) (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset)) / 2)
				+ DisplayUtils.widthOffset;
		scaledY = ((int) Math
				.round((double) (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset)) / 2)
				+ DisplayUtils.heightOffset;
				
		scaledRadius = (int) Math
				.round((double) radius / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}
	
	public double setRotation() {
		double delta = ((GLFW.glfwGetTime() * 1000) - lastControllerDelta);
		lastControllerDelta = GLFW.glfwGetTime() * 1000;
		
		if(ControllerUtils.isButtonPressed(Constants.CONTROLLER_MOVEMENT_ROTATION_CLOCKWISE)) {
			rotation = rotation + 0.5 * delta;
			
			return rotation;
		}
		if(ControllerUtils.isButtonPressed(Constants.CONTROLLER_MOVEMENT_ROTATION_COUNTERCLOCKWISE)) {
			rotation = rotation - 0.5 * delta;
			return rotation;
		}

		DoubleBuffer mxpos = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer mypos = BufferUtils.createDoubleBuffer(1);
		
		GLFW.glfwGetCursorPos(DisplayUtils.getHandle(), mxpos, mypos);
		
		double x = mxpos.get(0);
		double y = mypos.get(0);
		
		if(x == lastMouseX && y == lastMouseY) 
			return rotation;
		
		lastMouseX = x;
		lastMouseY = y;
		
		mxpos.clear();
		mypos.clear();
		
		IntBuffer xpos = BufferUtils.createIntBuffer(1);
		IntBuffer ypos = BufferUtils.createIntBuffer(1);
		
		
		GLFW.glfwGetWindowPos(DisplayUtils.getHandle(), xpos, ypos);
		
		x = x + xpos.get(0) + ((DisplayUtils.monitorWidth / 2) - xpos.get(0) - DisplayUtils.getDisplayWidth() / 2);
		y = -y + DisplayUtils.monitorHeight - ypos.get(0) - ((DisplayUtils.monitorHeight / 2) - ypos.get(0) - DisplayUtils.getDisplayHeight() / 2);
		
		xpos.clear();
		ypos.clear();
		
		double scaledX = x / GLFW.glfwGetVideoMode(DisplayUtils.monitor).width() * Camera.CAMERA_WIDTH;
		double scaledY = y / GLFW.glfwGetVideoMode(DisplayUtils.monitor).height() * Camera.CAMERA_HEIGHT;
		
		double m = (Camera.CAMERA_HEIGHT / 2 - scaledY) / (Camera.CAMERA_WIDTH / 2 - scaledX);
		
		if(m == Double.POSITIVE_INFINITY) {
			rotation = 90;
			return rotation;
		}
		
		if(m == Double.NEGATIVE_INFINITY) {
			rotation = 270;
			return rotation;
		}
		if(scaledX == Camera.CAMERA_WIDTH / 2 && scaledY == Camera.CAMERA_HEIGHT / 2)
			return rotation;

		if(scaledX < Camera.CAMERA_WIDTH / 2)
			rotation = 180 - Math.toDegrees(Math.atan(m));
		else {
			if(360 - Math.toDegrees(Math.atan(m)) > 360)
				rotation = 360 - Math.toDegrees(Math.atan(m)) - 360;
			else
				rotation = 360 - Math.toDegrees(Math.atan(m));
		}
		
		return rotation;
	}
	public void move(List<Room> rooms) {

		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		lastDelta = GLFW.glfwGetTime() * 1000;
		
		double speed = 0;
		
		if(KEYS_MOVEMENT_FASTER.isKeyDown()) {
			speed = Player.fastSpeed * delta;
		}
		else {
			speed = Player.slowSpeed * delta;
		}	
		
		boolean down = true;
		
		double tempAngle = -1;
		
		if(!(KEYS_MOVEMENT_UP.isKeyDown()) == KEYS_MOVEMENT_DOWN.isKeyDown()) {
			down = false;
			
			if(KEYS_MOVEMENT_UP.isKeyDown())
				tempAngle = 0;
			else
				tempAngle = 180;
		}
		if(!(KEYS_MOVEMENT_LEFT.isKeyDown() == KEYS_MOVEMENT_RIGHT.isKeyDown())) {
			down = false;
			
			if(KEYS_MOVEMENT_LEFT.isKeyDown())
				if(tempAngle == -1)
					tempAngle =  270;
				else if(KEYS_MOVEMENT_UP.isKeyDown())
					tempAngle = (tempAngle + 360 + 270) / 2;
				else
					tempAngle = (tempAngle + 270) / 2;
			else
				if(tempAngle == -1)
					tempAngle =  90;
				else 
					tempAngle = (tempAngle + 90) / 2;
		}
					
		if(down)
			speed = 0;		
		
		if(tempAngle == -1) tempAngle = 0;
		
		double angle = setRotation() + tempAngle;
		
		List<Double> verticalEdges = getVerticalEdges(rooms);
		List<Double> horizontalEdges = getHorizontalEdges(rooms);
		
		double x = 0;
		double y = 0;
		
		boolean bx = false;
		boolean by = false;
		
		boolean initX = true;
		boolean initY = true;
		
		for(double verticalEdge : verticalEdges) {
			double projectedX = this.getPosX() + speed * Math.cos(Math.toRadians(angle));
			
			if(!(this.getPosX() > verticalEdge - this.radius) && (projectedX >= verticalEdge - this.radius)) {
				bx = true;
				
				double temp = verticalEdge - this.radius;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}
			else if(!(this.getPosX() < verticalEdge + this.getRadius()) && (projectedX <= verticalEdge + this.getRadius())) {
				bx = true;
				
				double temp = verticalEdge + this.radius;
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}					
		}
		
		if(bx)
			this.setPosX(x);
		else
			this.setPosX(this.getPosX() + speed * Math.cos(Math.toRadians(angle)));
		
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
			double projectedY = this.getPosY() - speed * Math.sin(Math.toRadians(angle));
			
			if(!(this.getPosY() > horizontalEdge - this.radius) && (projectedY >= horizontalEdge - this.radius)) {
				by = true;
				
				double temp = horizontalEdge - this.radius;
				if(temp > y || initY) {
					y = temp;
					initY = false;
				}
			}
			else if(!(this.getPosY() < horizontalEdge + this.getRadius()) && (projectedY <= horizontalEdge + this.getRadius())) {
				by = true;
				
				double temp = horizontalEdge + this.radius;
				if(temp > y || initY) {
					y = temp;
					initY = false;
				}
			}					
		}
		
		if(by)
			this.setPosY(y);
		else
			this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		
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
		List<Double> verticalEdges = new ArrayList<Double>();
	
		for(Room room : rooms) {
			verticalEdges.add(room.getPosX());
			verticalEdges.add(room.getPosX() + room.getWidth());
		}
		
		return verticalEdges;
	}
	
	private List<Double> getHorizontalEdges(List<Room> rooms) {
		List<Double> horizontalEdges = new ArrayList<Double>();
		
		for(Room room : rooms) {
			horizontalEdges.add(room.getPosY() + room.getHeight());
			horizontalEdges.add(room.getPosY());
		}
		
		return horizontalEdges;
	}
}