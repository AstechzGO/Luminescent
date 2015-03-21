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


	static {
		// PWM
		defaults.put(LOG_CONFIG, "false");

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
