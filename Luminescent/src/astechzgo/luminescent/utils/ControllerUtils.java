package astechzgo.luminescent.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.lwjgl.glfw.GLFW;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

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
			LoggingUtils.printException(e);
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
					try{
						if(GLFWButtons.getInt(button) != GLFW.GLFW_PRESS) {
							areAllDown = false;
						}
					}
					catch(Exception e) {
						LoggingUtils.printException(e);
					}
				}
				else {
					// Decoding controller direction and axis
					// Sorry to anyone trying to read this code
					
					button = Math.abs(button);
						   
					int axis = (button / 10) - 1;
					int value = (button - 10 - (axis * 10)) - 2;
						   
					if(value == 0) value = -1;	// Because 0 means nothing
					
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
		List<List<List<Integer>>> buttonNumbers = null;
		
		File dir = newFile("controllers");
		File defaultConf = new File(dir, "defualt.properties");
		
		if(!dir.isDirectory())
			dir.mkdirs();
		
		if(!defaultConf.isFile()) {
			try {
				defaultConf.createNewFile();
				InputStream confIn = (new ControllerUtils()).getClass().getResourceAsStream("/resources/properties/default.properties");
				Files.copy(confIn, defaultConf.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				LoggingUtils.printException(e);
			}
		}
		
		String joystickName = GLFW.glfwGetJoystickName(joystick);
		int joystickButtons = GLFW.glfwGetJoystickButtons(joystick).capacity();
		int joystickAxes = GLFW.glfwGetJoystickAxes(joystick).capacity();
		
		File controllerPropertiesFile = 
				new File(dir, joystickName + "." + joystickButtons + "." + joystickAxes + ".properties");
		
		if(!controllerPropertiesFile.isFile()) {
			controllerPropertiesFile = defaultConf;
			
		}
		
		Properties p = new Properties();
		
		try {
			p.load(new FileInputStream(controllerPropertiesFile));
		} catch (IOException e) {
			LoggingUtils.printException(e);
		}
		
		buttonNumbers = parse(p.getProperty(button));

		return buttonNumbers;
	}
	
	private static List<List<List<Integer>>> parse(String string) {
			List<List<List<Integer>>> buttons = new ArrayList<List<List<Integer>>>(3);
			
			List<List<Integer>> temp = new ArrayList<List<Integer>>();
			buttons.add(0, temp);
			buttons.add(1, temp);
			buttons.add(2, temp);
			
			Scanner scanner = new Scanner(string);
			
			int q = SystemUtils.getCurrentOS();
			for (String a; (a = scanner.findWithinHorizon("(?<=\\{\\{).*?(?=\\}\\})", 0)) != null;) {
		    
				a = "{" + a + "}";
				
				Scanner sc = new Scanner(a);
				
				int i = 0;
				for (String s; (s = sc.findWithinHorizon("(?<=\\{).*?(?=\\})", 0)) != null; i++) {
					buttons.get(q).add(i, new ArrayList<Integer>());
					s = s.replace(" ", "");
					String[] unparsed = s.split(",");
					for(String uNum : unparsed) {
						buttons.get(q).get(i).add(Integer.parseInt(uNum));
					}
				}
				sc.close();
				break;
			}
			scanner.close();
			return buttons;
		}		
}