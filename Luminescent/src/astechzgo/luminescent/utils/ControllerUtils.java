package astechzgo.luminescent.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

public class ControllerUtils {
	
	public static List<Integer> joysticks = new ArrayList<Integer>();
	
	public static final List<Integer> JOYSTICK_SLOT_VALUES = getAllJoysticks();
	
	public static final int UP = 0;
	public static final int DOWN = 1;
	public static final int RIGHT = 2;
	public static final int LEFT = 3;
	
	public static final int UNKNOWN_DIRECTION = -1;
	
	public static void updateJoysticks() {
		for(int joystick : JOYSTICK_SLOT_VALUES) {
			if(GLFW.glfwJoystickPresent(joystick) == 1) {
				if(!joysticks.contains(joystick)) {
					joysticks.add(joystick);
				}
			}
			else {
				if(joysticks.contains(joystick)) {
					joysticks.remove(joystick);
				}
			}
		}
	}
	
	private static List<Integer> getAllJoysticks() {
		// Use reflection to find out key names
		Field[] fields = GLFW.class.getFields();
						
		List<Integer> joysticks = new ArrayList<Integer>();
		try {
			for ( Field field : fields ) {
				if ( Modifier.isStatic(field.getModifiers())
					&& Modifier.isPublic(field.getModifiers())
					&& Modifier.isFinal(field.getModifiers())
					&& field.getType().equals(int.class)
					&& field.getName().startsWith("GLFW_JOYSTICK_") ) {
									
					joysticks.add(field.getInt(null));
				}
			}
			return joysticks;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;	
	}
	
	public static boolean isButtonPressed(String button) {
		int buttonNumber = -1;
		int analogDirection = UNKNOWN_DIRECTION;
		
		switch(button) {
			case Constants.KEYS_MOVEMENT_DOWN:
				analogDirection = DOWN;
				break;
			case Constants.KEYS_MOVEMENT_FASTER:
				buttonNumber = 0;
				break;
			case Constants.KEYS_MOVEMENT_LEFT:
				analogDirection = LEFT;
				break;
			case Constants.KEYS_MOVEMENT_RIGHT:
				analogDirection = RIGHT;
				break;
			case Constants.KEYS_MOVEMENT_UP:
				analogDirection = UP;
				break;
			case Constants.KEYS_UTIL_EXIT:
				buttonNumber = 4;
				break;
			case Constants.KEYS_UTIL_FULLSCREEN:
				buttonNumber = 7;
				break;
			case Constants.KEYS_UTIL_SCREENSHOT:
				buttonNumber = 5;
				break;
			case Constants.KEYS_UTIL_NEXTWINDOW:
				buttonNumber = 6;
				break;
			default:
				buttonNumber = -1;
				break;
		}
		
		for(int joy : joysticks) {
			if(isButtonPressed(joy, buttonNumber)) 
				return true;
			else if(analogDirection != UNKNOWN_DIRECTION) {
				FloatBuffer axes = GLFW.glfwGetJoystickAxes(joy);
				
				double directionX = axes.get(0);
				double directionY = axes.get(1);
				
				return isAnalogStickDirection(directionX, directionY, analogDirection);
			}
		}
		return false;
	}
	
	private static boolean isButtonPressed(int joystick, int button) {
		if(button == -1)
			return false;
		
		ByteBuffer buttons = GLFW.glfwGetJoystickButtons(joystick);
		
		if(buttons.getInt(button) == GLFW.GLFW_PRESS)
			return true;
		else
			return false;
		
	}
	
	private static boolean isAnalogStickDirection(double x, double y, int direction) {
		switch(direction) {
			case UP:
				if(y <= -1.0) return true; else return false;
			case DOWN:
				if(y >= 1.0) return true; else return false;
			case RIGHT:
				if(x >= 1.0) return true; else return false;
			case LEFT:
				if(x <= -1.0) return true; else return false;
		}
		
		return false;
	}
}