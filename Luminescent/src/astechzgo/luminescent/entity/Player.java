package astechzgo.luminescent.entity;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.KeyboardUtils;

public class Player extends CircularEntity implements LivingEntity{
	private double lastDelta;
	
	private double lastControllerDelta = 0;
	private double lastMouseX = 0;
	private double lastMouseY = 0;
	
	private int health;	
	private boolean alive;
	
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
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_FASTER)) {
			Luminescent.moveSpeed = 0.88;
		}
		else {
			Luminescent.moveSpeed = 0.5;
		}
		
		double speed = Luminescent.moveSpeed * delta;	
		
		boolean down = true;
		
		double tempAngle = -1;
		
		if(!(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP) == KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN))) {
			down = false;
			
			if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
				tempAngle = 0;
			else
				tempAngle = 180;
		}
		if(!(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT) == KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT))) {
			down = false;
			
			if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT))
				if(tempAngle == -1)
					tempAngle =  270;
				else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP))
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
		
		double x = 0;
		double y = 0;
		
		boolean bx = false;
		boolean by = false;
		
		boolean initX = true;
		boolean initY = true;
		
		for(Room room : rooms) {
			if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - this.getRadius()) {
				bx = true;
				
				double temp = room.getPosX() + room.getWidth() - this.getRadius();
				if(temp > x || initX) {
					x = temp;
					initX = false;
				}
			}
			else if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + this.getRadius()) {
				bx = true;
				
				double temp = room.getPosX() + this.getRadius();
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
		
		for(Room room : rooms) {
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
		}
		
		if(by)
			this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		else
			this.setPosY(y);
		
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

	@Override
	public int getHealth() {
		return health;
	}

	@Override
	public void setHealth(int health) {
		this.health = health;
		
	}

	@Override
	public boolean isAlive() {
		return alive;
	}
	
	
}