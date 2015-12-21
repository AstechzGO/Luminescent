package astechzgo.luminescent.entity;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.KeyboardUtils;

public class Player extends CircularEntity {
	private double lastDelta;
	
	public Player() {
		super(1920 / 2, 1080 / 2, 40, 1);
		lastDelta = GLFW.glfwGetTime() * 1000;
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
				.round((double) radius / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}
	
	public double setRotation() {		
		DoubleBuffer mxpos = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer mypos = BufferUtils.createDoubleBuffer(1);
		
		GLFW.glfwGetCursorPos(DisplayUtils.getHandle(), mxpos, mypos);
		
		double x = mxpos.get(0);
		double y = mypos.get(0);
		
		mxpos.clear();
		mypos.clear();
		
		IntBuffer xpos = BufferUtils.createIntBuffer(1);
		IntBuffer ypos = BufferUtils.createIntBuffer(1);
		
		
		GLFW.glfwGetWindowPos(DisplayUtils.getHandle(), xpos, ypos);
		
		x = x + xpos.get(0) + ((1920 / 2) - xpos.get(0) - DisplayUtils.getDisplayWidth() / 2);
		y = -y + DisplayUtils.monitorHeight - ypos.get(0) - ((1080 / 2) - ypos.get(0) - DisplayUtils.getDisplayHeight() / 2);
		
		xpos.clear();
		ypos.clear();
		
		double scaledX = x / GLFW.glfwGetVideoMode(DisplayUtils.monitor).width() * 1920;
		double scaledY = y / GLFW.glfwGetVideoMode(DisplayUtils.monitor).height() * 1080;
		
		double m = (1080 / 2 - scaledY) / (1920 / 2 - scaledX);
		
		if(m == Double.POSITIVE_INFINITY) {
			rotation = 90;
			return rotation;
		}
		
		if(m == Double.NEGATIVE_INFINITY) {
			rotation = 270;
			return rotation;
		}
		if(scaledX == 1920 / 2 && scaledY == 1080 / 2)
			return rotation;

		if(scaledX < 1920 / 2)
			rotation =  360 - Math.toDegrees(Math.atan(m) - 135);
		else
			rotation = 360 - Math.toDegrees(Math.atan(m));
		return rotation;
	}
	public void move(Room room) {

		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		lastDelta = GLFW.glfwGetTime() * 1000;
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_FASTER)) {
			Luminescent.moveSpeed = 0.88;
		}
		else {
			Luminescent.moveSpeed = 0.5;
		}
		
		double speed = Luminescent.moveSpeed *  delta;
		
		double angle = this.setRotation();		
		
		if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_UP)) {
			if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - this.getRadius())
				this.setPosX(room.getPosX() + room.getWidth() - this.getRadius());	
			else if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + this.getRadius())
				this.setPosX(room.getPosX() + this.getRadius());
			else
				this.setPosX(this.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - this.getRadius())
				this.setPosY(room.getPosY() + room.getHeight() - this.getRadius());
			else if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + this.getRadius())
				this.setPosY(room.getPosY() + this.getRadius());
			else
				this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_DOWN)) {
			speed = -speed;
			if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - this.getRadius())
				this.setPosX(room.getPosX() + room.getWidth() - this.getRadius());		
			else if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + this.getRadius())
				this.setPosX(room.getPosX() + this.getRadius());
			else
				this.setPosX(this.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - this.getRadius())
				this.setPosY(room.getPosY() + room.getHeight() - this.getRadius());
			else if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + this.getRadius())
				this.setPosY(room.getPosY() + this.getRadius());
			else
				this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_RIGHT)) {
			angle = angle + 90;
			if((this.getPosX() + speed * Math.cos(Math.toRadians(angle))) >= room.getPosX() + room.getWidth() - this.getRadius())
				this.setPosX(room.getPosX() + room.getWidth() - this.getRadius());
			else if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + this.getRadius())
				this.setPosX(room.getPosX() + this.getRadius());
			else
				this.setPosX(this.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - this.getRadius())
				this.setPosY(room.getPosY() + room.getHeight() - this.getRadius());
			else if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + this.getRadius())
				this.setPosY(room.getPosY() + this.getRadius());
			else
				this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
		}
		else if(KeyboardUtils.isKeyDown(Constants.KEYS_MOVEMENT_LEFT)) {
			angle = angle - 90;
			if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) >= room.getPosX() + room.getWidth() - this.getRadius())
				this.setPosX(room.getPosX() + room.getWidth() - this.getRadius());		
			else if(this.getPosX() + speed * Math.cos(Math.toRadians(angle)) <= room.getPosX() + this.getRadius())
				this.setPosX(room.getPosX() + this.getRadius());
			else
				this.setPosX(this.getPosX() + speed * Math.cos(Math.toRadians(angle)));
			
			if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) >= room.getPosY() + room.getHeight() - this.getRadius())
				this.setPosY(room.getPosY() + room.getHeight() - this.getRadius());
			else if(this.getPosY() - speed * Math.sin(Math.toRadians(angle)) <= room.getPosY() + this.getRadius())
				this.setPosY(room.getPosY() + this.getRadius());
			else
				this.setPosY(this.getPosY() - speed * Math.sin(Math.toRadians(angle)));
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
	
	
}