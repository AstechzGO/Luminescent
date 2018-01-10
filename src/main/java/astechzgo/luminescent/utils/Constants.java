package astechzgo.luminescent.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class Constants
{
	private static final String CONSTANTS_FILE_NAME = 							"conf.properties";
	
	private static final Properties defaults = new Properties();
	private static final Properties constants = new Properties();
	
	public static final String LOG_CONFIG =										"Log-Debug-Messages";
	
	public static final String KEYS_MOVEMENT_FASTER =							"keys.movement.faster";			
	public static final String KEYS_MOVEMENT_UP =								"keys.movement.up";
	public static final String KEYS_MOVEMENT_LEFT = 							"keys.movement.left";
	public static final String KEYS_MOVEMENT_DOWN =								"keys.movement.down";
	public static final String KEYS_MOVEMENT_RIGHT = 							"keys.movement.right";

	public static final String KEYS_NEURAL_LOSE =								"keys.neural.lose";
	
	public static final String KEYS_ACTION_SHOOT =								"keys.action.shoot";
	
	public static final String KEYS_UTIL_SCREENSHOT =							"keys.util.screenshot";
	public static final String KEYS_UTIL_FULLSCREEN = 							"keys.util.fullscreen";
	public static final String KEYS_UTIL_EXIT = 								"keys.util.exit";
	public static final String KEYS_UTIL_NEXTWINDOW = 							"keys.utils.nextwindow";
	
	public static final String CONTROLLER_MOVEMENT_ROTATION_CLOCKWISE = 		"controller.movement.rotation.clockwise"; 
	public static final String CONTROLLER_MOVEMENT_ROTATION_COUNTERCLOCKWISE = 	"controller.movement.rotation.counterclockwise"; 
	
	public static final String WINDOW_FULLSCREEN = 								"window.fullscreen";


	static {
		defaults.put(LOG_CONFIG, "false");
		

		
		defaults.put(KEYS_MOVEMENT_FASTER,										"{LEFT_SHIFT}");
		defaults.put(KEYS_MOVEMENT_UP, 											"{W}, {UP}");
		defaults.put(KEYS_MOVEMENT_LEFT, 										"{A}, {LEFT}");
		defaults.put(KEYS_MOVEMENT_DOWN, 										"{S}, {DOWN}");
		defaults.put(KEYS_MOVEMENT_RIGHT,										"{D}, {RIGHT}");

		defaults.put(KEYS_NEURAL_LOSE,											"{l}");

		defaults.put(KEYS_ACTION_SHOOT,											"{SPACE}");
		
		defaults.put(KEYS_UTIL_SCREENSHOT, 										"{F2}");
		defaults.put(KEYS_UTIL_FULLSCREEN, 										"{F10}");
		defaults.put(KEYS_UTIL_EXIT, 											"{TAB, ESCAPE}");
		defaults.put(KEYS_UTIL_NEXTWINDOW,										"{F4}");
		
		defaults.put(CONTROLLER_MOVEMENT_ROTATION_CLOCKWISE,					"{{-1}},{{-1}},{{-1}}");
		defaults.put(CONTROLLER_MOVEMENT_ROTATION_COUNTERCLOCKWISE,				"{{-1}},{{-1}},{{-1}}");
		
		defaults.put(WINDOW_FULLSCREEN,											"false");

		constants.putAll(defaults);
	}
	
	/**
	 * Returns constant as a String
	 * @param name name
	 * @return Constant as a String
	 */
	public static String getConstant(String name) {
		return constants.getProperty(name);
	}

	public static void setConstant(String name, String value) {
		constants.setProperty(name, value);
	}

	/**
	 * Returns constant as an int
	 * @param name name
	 * @return Constant as an int
	 */
	public static int getConstantAsInt(String name) {
		return Integer.parseInt(constants.getProperty(name));
	}
	
	/**
	 * Returns constant as a double
	 * @param name name
	 * @return Constant as an double
	 */
	public static double getConstantAsDouble(String name) {
		return Double.parseDouble(constants.getProperty(name));
	}
	
	/**
	 * Returns constant as a boolean
	 * @param name name
	 * @return Constant as a boolean
	 */
	public static boolean getConstantAsBoolean(String name) {
        return constants.getProperty(name).toLowerCase().equals("true");
	}
	
	public static void readConstantPropertiesFromFile() {
		if(!newFile(CONSTANTS_FILE_NAME).exists()) {
			try {
				newFile(CONSTANTS_FILE_NAME).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		Properties defaultsFromFile = new Properties();
		FileInputStream in = null;
		try {
			in = new FileInputStream(newFile(CONSTANTS_FILE_NAME));			
			defaultsFromFile.load(in);
		} catch (IOException e) {
			System.out.println("Warning: Unable to load properties file " + CONSTANTS_FILE_NAME);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException e) {
				System.out.println("Error: Unable to close properties file " + CONSTANTS_FILE_NAME);
				e.printStackTrace();
			}			
		}
		
		constants.putAll(defaultsFromFile);
	}
}
