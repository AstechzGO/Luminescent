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
		for(int joy : joysticks) {
			List<List<List<Integer>>> buttonNumbers = getButtons(joy, button);
			if(isButtonPressed(joy, buttonNumbers)) 
				return true;
			else
				return false;
		}
		return false;
	}
	
	private static boolean isButtonPressed(int joystick, List<List<List<Integer>>> buttons) {
		if(buttons == null) {
			return false;
		}
		
		ByteBuffer GLFWButtons = GLFW.glfwGetJoystickButtons(joystick);
		
		for(List<Integer> rButtons : buttons.get(SystemUtils.getCurrentOS())) {
			boolean areAllDown = true;
			for(Integer button : rButtons) {
				if(button >= -1) {
					if(GLFWButtons.getInt(button) != GLFW.GLFW_PRESS) {
						areAllDown = false;
					}
				}
				else {
					//Decoding controller direction and axis
					//Sorry to anyone trying to read this code
					
					button = Math.abs(button);
						   
					int axis = (button / 10) - 1;
					int value = (button - 10 - (axis * 10)) - 2;
						   
					if(value == 0) value = -1;	//Because 0 means nothing
					
					FloatBuffer GLFWAxis = GLFW.glfwGetJoystickAxes(joystick);
					
					double actualValue = GLFWAxis.get(axis);
					
					if(value < 0) {
						if(actualValue > value)
							areAllDown = false;
					}
					else {
						if(actualValue < value)
							areAllDown = false;
					}
				}
			}
			if(areAllDown) {
				return true;
			}
		}	
		return false;
	}
	
	private static List<List<List<Integer>>> getButtons(int joystick, String button) {
		List<List<List<Integer>>> buttonNumbers = new ArrayList<List<List<Integer>>>();
		
		List<Integer> temp = null;
		
		buttonNumbers.add(new ArrayList<List<Integer>>());
		buttonNumbers.add(new ArrayList<List<Integer>>());
		buttonNumbers.add(new ArrayList<List<Integer>>());
		
		switch(button) {
			case Constants.KEYS_MOVEMENT_DOWN:
				temp = new ArrayList<Integer>();
				temp.add(-23);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_MOVEMENT_FASTER:
				temp = new ArrayList<Integer>();
				temp.add(8);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_MOVEMENT_LEFT:
				temp = new ArrayList<Integer>();
				temp.add(-12);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_MOVEMENT_RIGHT:
				temp = new ArrayList<Integer>();
				temp.add(-13);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_MOVEMENT_UP:
				temp = new ArrayList<Integer>();
				temp.add(-22);
				buttonNumbers.get(0).add(temp);
				
				temp = new ArrayList<Integer>();
				temp.add(10);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_UTIL_EXIT:
				temp = new ArrayList<Integer>();
				temp.add(4);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_UTIL_FULLSCREEN:
				temp = new ArrayList<Integer>();
				temp.add(7);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_UTIL_SCREENSHOT:
				temp = new ArrayList<Integer>();
				temp.add(5);
				buttonNumbers.get(0).add(temp);
				break;
			case Constants.KEYS_UTIL_NEXTWINDOW:
				temp = new ArrayList<Integer>();
				temp.add(6);
				buttonNumbers.get(0).add(temp);
				break;
			default:
				buttonNumbers = null;
				break;
		}
		return buttonNumbers;
	}
}