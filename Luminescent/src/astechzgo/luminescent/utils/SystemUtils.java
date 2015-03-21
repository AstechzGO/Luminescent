package astechzgo.luminescent.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.lwjgl.input.Keyboard;

public class SystemUtils {

	private static String userHome = System.getProperty("user.home", ".");
	private static File workingDirectory;
	
	/**
	 * When not deployed, this game only supports windows in the Eclipse IDE
	 * 
	 * When deployed, this game can be run as an executable JAR file in
	 * Windows, Linux, or OS X
	 */
	public static void doOSSetUp() {
		
		//Windows
		if(System.getProperty("os.name").toLowerCase().contains("win")) {
				
			      String applicationData = System.getenv("APPDATA");
			      String folder = applicationData != null ? applicationData : userHome;

			      workingDirectory = new File(folder, ".luminescent/");

		}
		
		//Linux/Unix
		else if(System.getProperty("os.name").toLowerCase().contains("unix")
				|| System.getProperty("os.name").toLowerCase().contains("linux")) {
			
				workingDirectory = new File(userHome, ".luminescent/");
		}
		
		//Mac
		else if(System.getProperty("os.name").toLowerCase().contains("mac")) {
			workingDirectory = new File(userHome, "Library/Application Support/luminescent");
		}
		
		//Unkown
		else {
			System.err.println("The current platform is not supported");
			System.exit(-1);			
		}

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
	
	private static List<List<Integer>> getKeyCodes(String key) {
		List<List<Integer>> keys = new ArrayList<List<Integer>>();
		
	    @SuppressWarnings("resource")
		Scanner sc = new Scanner(Constants.getConstant(key));
	    int i = 0;
	    for (String s; (s = sc.findWithinHorizon("(?<=\\{).*?(?=\\})", 0)) != null; i++) {
	    	keys.add(i, new ArrayList<Integer>());
	    	s = s.replace(" ", "");
	    	String[] unparsed = s.split(",");
	    	for(String uNum : unparsed) {
	    		Integer w = Integer.parseInt(uNum);
	    		keys.get(i).add(w);
	    	}
	    }
	    
		return keys;
	}
	
	public static boolean isKeyDown(String name) {
		List<List <Integer>> keys = getKeyCodes(name);

		for(List<Integer> rKeys : keys) {
			boolean areAllDown = true;
			for(Integer key : rKeys) {
				if(!Keyboard.isKeyDown(key)) {
					areAllDown = false;
				}
			}
			if(areAllDown) {
				return true;
			}
		}
		
		return false;
	}
}
