package astechzgo.luminescent.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.lwjgl.system.Platform;

public class SystemUtils {

	private static String userHome = System.getProperty("user.home", ".");
	private static File oldWorkingDirectory;
	private static File workingDirectory;
	
	/**
	 * When not deployed, this game only supports windows in the Eclipse IDE
	 * 
	 * When deployed, this game can be run as an executable JAR file in
	 * Windows, Linux, or OS X
	 */
	public static void doOSSetUp() {
		
		if(Platform.get() == Platform.LINUX) {
			workingDirectory = new File(userHome, ".luminescent/");
		}
		else if(Platform.get() == Platform.MACOSX) {
			workingDirectory = new File(userHome, "Library/Application Support/luminescent");
		}
		else if(Platform.get() == Platform.WINDOWS) {
			String applicationData = System.getenv("APPDATA");
			String folder = applicationData != null ? applicationData : userHome;

			workingDirectory = new File(folder, ".luminescent/");
		}		
		else {
			System.err.println("The current platform is not supported");
			System.exit(-1);
		}

		oldWorkingDirectory = new File(System.getProperty("user.dir"));
		
		setWorkingDirectory(workingDirectory.getPath());	
	}
	
	private static boolean setWorkingDirectory(String directoryName) {
        boolean result = false;  // Boolean indicating whether directory was set
        File    directory;       // Desired current working directory

        directory = new File(directoryName).getAbsoluteFile();
        if (directory.exists() || directory.mkdirs()) {
            result = (System.setProperty("user.dir", directory.getAbsolutePath()) != null);
        }

        return result;
    }
	
	public static File newFile(String relativeLoc) {
		return new File(new File(relativeLoc).getAbsolutePath());
	}
	
	public static URL getResourceAsURL(String relativeLoc) {
	    if(isJar()) {
	        return SystemUtils.class.getResource("/resources/" + relativeLoc);
	    }
	    else {
	        try {
                return new File(oldWorkingDirectory, "src/main/resources/" + relativeLoc).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
	        return SystemUtils.class.getResource("/resources/" + relativeLoc);
	    }
	}
	
	public static boolean isJar() {
	    return SystemUtils.class.getResource("SystemUtils.class").getProtocol().equalsIgnoreCase("JAR");
	}
}
