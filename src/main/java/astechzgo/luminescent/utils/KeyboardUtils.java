package astechzgo.luminescent.utils;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.system.APIUtil;

public class KeyboardUtils {
	
	private static final Map<Integer, String> KEY_CODES = APIUtil.apiClassTokens((field, value) -> field.getName().startsWith("GLFW_KEY_"), null, GLFW.class);
	
	private static final List<Integer> keysPressed = new ArrayList<>();
	
	public static final GLFWKeyCallback KEY_CALLBACK = new GLFWKeyCallback() {
		@Override
		public void invoke(long window, int key, int scancode, int action, int mods) {		
			if(action == GLFW.GLFW_PRESS) {
				keysPressed.add(key);
			}
			else if (action == GLFW_RELEASE) {
				keysPressed.remove((Integer)key);
			}
		}
	};
	
	static {
		for (Entry<Integer, String> entry : KEY_CODES.entrySet()) {
            entry.setValue(entry.getValue().substring(9));
        }
		
		GLFW.glfwSetKeyCallback(DisplayUtils.getHandle(), KEY_CALLBACK);
	}
	
	private static int getKeyNumber(String keyname) {
		for (Entry<Integer, String> entry : KEY_CODES.entrySet()) {
            if (entry.getValue().equals(keyname)) {
                return entry.getKey();
            }
        }
		
		return GLFW.GLFW_KEY_UNKNOWN;
	}
	
	@SuppressWarnings("unused")
	private static String getKeyName(int keynumber) {
		return KEY_CODES.get(keynumber);
	}
	
	private static boolean isKeyDown(int key) {
		return keysPressed.contains(key);
	}
	

	private static List<List<Integer>> getKeyCodes(String key) {
		List<List<Integer>> keys = new ArrayList<>();
		
		Scanner sc = new Scanner(Constants.getConstant(key));
	    int i = 0;
	    for (String s; (s = sc.findWithinHorizon("(?<=\\{).*?(?=})", 0)) != null; i++) {
	    	keys.add(i, new ArrayList<>());
	    	s = s.replace(" ", "");
	    	String[] unparsed = s.split(",");
	    	for(String uNum : unparsed) {
	    		Integer w = KeyboardUtils.getKeyNumber(uNum.toUpperCase());
	    		keys.get(i).add(w);
	    	}
	    }
	    
	    sc.close();
	    
		return keys;
	}
	
	public static boolean isKeyDown(String name) {
		List<List <Integer>> keys = getKeyCodes(name);

		for(List<Integer> rKeys : keys) {
			boolean areAllDown = true;
			for(Integer key : rKeys) {
				if(!isKeyDown(key)) {
					areAllDown = false;
				}
			}
			if(areAllDown) {
				return true;
			}
		}
		
		return ControllerUtils.isButtonPressed(name);
	}
	
	public static void resetKeys() {
		keysPressed.clear();
	}
}
