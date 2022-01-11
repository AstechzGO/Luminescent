package astechzgo.luminescent.utils;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.APIUtil;
import org.lwjgl.system.Platform;

public class ControllerUtils {

	public static final List<Integer> joysticks = new ArrayList<>();

	public static final Map<Integer, String> JOYSTICK_SLOT_VALUES = APIUtil.apiClassTokens((field, value) -> field.getName().matches("^GLFW_JOYSTICK_*\\d\\d?$"), null, GLFW.class);

	private static Properties cachedProperties;

	public static void updateJoysticks() {
		for(Entry<Integer, String> joystickEntry : JOYSTICK_SLOT_VALUES.entrySet()) {
			int joystick = joystickEntry.getKey();
			if(GLFW.glfwJoystickPresent(joystick)) {
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

	public static boolean isButtonPressed(String button) {
		for(int joy : joysticks) {
			List<List<Double>> buttonNumbers = getButtons(joy, button);

			boolean pressed = isButtonPressed(joy, buttonNumbers);
			if(pressed) {
			    return pressed;
            }
		}
		return false;
	}
	
	private static boolean isButtonPressed(int joystick, List<List<Double>> buttons) {
		if(buttons == null) {
			return false;
		}

		ByteBuffer GLFWButtons = GLFW.glfwGetJoystickButtons(joystick);
		// Joystick was disconnected while processing inputs
		if (GLFWButtons == null) return false;
		
		for(List<Double> rButtons : buttons) {
			boolean areAllDown = true;
			for(Double button : rButtons) {
				if(button >= -1) {
					try{
						if(GLFWButtons.getInt(button.intValue()) != GLFW.GLFW_PRESS) {
							areAllDown = false;
						}
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				}
				else {
					// Decoding controller direction and axis
					// Sorry to anyone trying to read this code

					button = Math.abs(button);

					int axis = (int)(button / 10) - 1;
					double value = (button - 10 - (axis * 10)) - 2;

					if(value == 0) value = -1;	// Because 0 means nothing

					FloatBuffer GLFWAxis = GLFW.glfwGetJoystickAxes(joystick);
					// Joystick was disconnected while processing inputs
					if (GLFWAxis == null) return false;

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
	private static List<List<Double>> getButtons(int joystick, String button) {
		List<List<Double>> buttonNumbers = null;
		

		if(cachedProperties != null) {
			return parse(cachedProperties.getProperty(button));
		}


		File dir = newFile("controllers");
		File defaultConf = new File(dir, "defualt.properties");

		if(!dir.isDirectory())
			dir.mkdirs();

		if(!defaultConf.isFile()) {
			try {
				defaultConf.createNewFile();
				InputStream confIn = getResourceAsURL("properties/default.properties").openStream();
				Files.copy(confIn, defaultConf.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
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
		cachedProperties = p;

		try {
			p.load(new FileInputStream(controllerPropertiesFile));
		} catch (IOException e) {
			e.printStackTrace();
		}

		buttonNumbers = parse(p.getProperty(button));

		return buttonNumbers;
	}
	
	private static List<List<Double>> parse(String string) {
        List<List<Double>> buttons = new ArrayList<>(3);

        Scanner scanner = new Scanner(string);
			
        int q = Platform.get().ordinal();

        String a = null;
        for(int i = 0; i <= q; i++) {
			a = scanner.findWithinHorizon("(?<=\\[).*?(?=])", 0);
		}

        Scanner sc = new Scanner(a);

        int i = 0;
        String s;
        while ((s = sc.findWithinHorizon("(?<=\\{).*?(?=})", 0)) != null) {
            buttons.add(i, new ArrayList<>());
            s = s.replace(" ", "");
            String[] unparsed = s.split(",");
            for(String uNum : unparsed) {
                buttons.get(i).add(Double.parseDouble(uNum));
            }
            i++;
        }
        sc.close();
        scanner.close();

        return buttons;
    }
}