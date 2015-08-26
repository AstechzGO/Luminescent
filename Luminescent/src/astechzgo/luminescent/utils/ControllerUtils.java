package astechzgo.luminescent.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
		return false;
	}
}