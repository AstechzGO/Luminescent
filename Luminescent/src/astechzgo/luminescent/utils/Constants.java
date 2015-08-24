package astechzgo.luminescent.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class Constants
{
	private static final String CONSTANTS_FILE_NAME = 	"conf.properties";
	
	private static Properties defaults = new Properties();
	private static Properties constants = new Properties();
	
	public static final String LOG_CONFIG =				"Log-Debug-Messages";
	
	public static final String KEYS_MOVEMENT_FASTER =	"keys.movement.faster";			
	public static final String KEYS_MOVEMENT_UP =		"keys.movement.up";
	public static final String KEYS_MOVEMENT_LEFT = 	"keys.movement.left";
	public static final String KEYS_MOVEMENT_DOWN =		"keys.movement.down";
	public static final String KEYS_MOVEMENT_RIGHT = 	"keys.movement.right";
	
	public static final String KEYS_UTIL_SCREENSHOT =	"keys.util.screenshot";
	public static final String KEYS_UTIL_FULLSCREEN = 	"keys.util.fullscreen";
	public static final String KEYS_UTIL_EXIT = 		"keys.util.exit";
	public static final String KEYS_UTIL_NEXTWINDOW = 	"keys.utils.nextwindow";
	
	public static final String WINDOW_FULLSCREEN = 		"window.fullscreen";


	static {
		defaults.put(LOG_CONFIG, "false");
		
		defaults.put(KEYS_MOVEMENT_FASTER,				"{LEFT_SHIFT}");
		defaults.put(KEYS_MOVEMENT_UP, 					"{W}, {UP}");
		defaults.put(KEYS_MOVEMENT_LEFT, 				"{A}, {LEFT}");
		defaults.put(KEYS_MOVEMENT_DOWN, 				"{S}, {DOWN}");
		defaults.put(KEYS_MOVEMENT_RIGHT, 				"{D}, {RIGHT}");
		
		defaults.put(KEYS_UTIL_SCREENSHOT, 				"{F2}");
		defaults.put(KEYS_UTIL_FULLSCREEN, 				"{F11}");
		defaults.put(KEYS_UTIL_EXIT, 					"{TAB, ESCAPE}");
		defaults.put(KEYS_UTIL_NEXTWINDOW,				"{F4}");
		
		defaults.put(WINDOW_FULLSCREEN,					"false");

		constants.putAll(defaults);
	}
	
	/**
	 * Returns constant as a String
	 * @param constant name
	 * @return
	 */
	public static String getConstant(String name) {
		return constants.getProperty(name);
	}

	public static void setConstant(String name, String value) {
		constants.setProperty(name, value);
	}

	/**
	 * Returns constant as an int
	 * @param constant name
	 * @return
	 */
	public static int getConstantAsInt(String name) {
		return Integer.parseInt(constants.getProperty(name));
	}
	
	/**
	 * Returns constant as a double
	 * @param constant name
	 * @return
	 */
	public static double getConstantAsDouble(String name) {
		return Double.parseDouble(constants.getProperty(name));
	}
	
	/**
	 * Returns constant as a boolean
	 * @param constant name
	 * @return
	 */
	public static boolean getConstantAsBoolean(String name) {
		if(constants.getProperty(name).toLowerCase().equals("true")) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void readConstantPropertiesFromFile() {
		if(!newFile(CONSTANTS_FILE_NAME).exists()) {
			try {
				newFile(CONSTANTS_FILE_NAME).createNewFile();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				LoggingUtils.logException(LoggingUtils.LOGGER, e1);
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
				LoggingUtils.logException(LoggingUtils.LOGGER, e);
			}			
		}
		
		constants.putAll(defaultsFromFile);
	}
}
