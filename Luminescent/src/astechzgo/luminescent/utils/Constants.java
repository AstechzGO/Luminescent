package astechzgo.luminescent.utils;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static astechzgo.luminescent.utils.SystemUtils.newFile;

public class Constants
{
	private static final String CONSTANTS_FILE_NAME = 			"conf.properties";
	
	private static Properties defaults = new Properties();
	private static Properties constants = new Properties();
	
	public static final String LOG_CONFIG =				"Log-Config-Messages";
	
	public static final String KEYS_MOVEMENT_FASTER =	"keys.movement.faster";			
	public static final String KEYS_MOVEMENT_UP =		"keys.movement.up";
	public static final String KEYS_MOVEMENT_LEFT = 	"keys.movement.left";
	public static final String KEYS_MOVEMENT_DOWN =		"keys.movement.down";
	public static final String KEYS_MOVEMENT_RIGHT = 	"keys.movement.right";
	
	public static final String KEYS_UTIL_SCREENSHOT =	"keys.util.screenshot";
	public static final String KEYS_UTIL_FULLSCREEN = 	"keys.util.fullscreen";
	public static final String KEYS_UTIL_EXIT = 		"keys.util.exit";


	static {
		defaults.put(LOG_CONFIG, "false");
		
		defaults.put(KEYS_MOVEMENT_FASTER, "{42}");
		defaults.put(KEYS_MOVEMENT_UP, "{17}, {200}");
		defaults.put(KEYS_MOVEMENT_LEFT, "{30}, {203}");
		defaults.put(KEYS_MOVEMENT_DOWN, "{31}, {208}");
		defaults.put(KEYS_MOVEMENT_RIGHT, "{32}, {205}");
		
		defaults.put(KEYS_UTIL_SCREENSHOT, "{60}");
		defaults.put(KEYS_UTIL_FULLSCREEN, "{87}");
		defaults.put(KEYS_UTIL_EXIT, "{1, 15}");

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
