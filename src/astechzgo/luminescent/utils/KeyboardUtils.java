package astechzgo.luminescent.utils;

import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardUtils {
	
	private static final String[] keyName = genKeyNames();
	private static final Map<String, Integer> keyMap = genKeyMap();
	
	private static final List<Integer> keysPressed = new ArrayList<Integer>();
	
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
	
	static{
		GLFW.glfwSetKeyCallback(DisplayUtils.getHandle(), KEY_CALLBACK);
	}
	
	private static String[] genKeyNames() {
		// Use reflection to find out key names
		Field[] fields = GLFW.class.getFields();
		
		List<String> keys = new ArrayList<String>();
		try {
			for ( Field field : fields ) {
				if ( Modifier.isStatic(field.getModifiers())
				     && Modifier.isPublic(field.getModifiers())
				     && Modifier.isFinal(field.getModifiers())
				     && field.getType().equals(int.class)
				     && field.getName().startsWith("GLFW_KEY_")
				     && !field.getName().endsWith("WIN") ) { /* Don't use deprecated names */
					
					String name = field.getName().substring(9);
					
					keys.add(name);
				}

			}
			return keys.toArray(new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private static Map<String, Integer> genKeyMap() {
		// Use reflection to find out key names
		Field[] fields = GLFW.class.getFields();
		
		Map<String, Integer> keyMap = null;
		
		List<String> names = new ArrayList<String>();
		List<Integer> keys = new ArrayList<Integer>();
		
		try {
			for ( Field field : fields ) {
				if ( Modifier.isStatic(field.getModifiers())
				     && Modifier.isPublic(field.getModifiers())
				     && Modifier.isFinal(field.getModifiers())
				     && field.getType().equals(int.class)
				     && field.getName().startsWith("GLFW_KEY_")
				     && !field.getName().endsWith("WIN") ) { /* Don't use deprecated names */

					int key = field.getInt(null);
					String name = field.getName().substring(9);
					
					names.add(name);
					keys.add(key);
					
					//keyMap.put(name, key);
				}
			}
			keyMap = new HashMap<String, Integer>(names.size());
			
			for(int i = 0; i < keys.size(); i++) {
				keyMap.put(names.get(i), keys.get(i));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return keyMap;
	}
	
	private static int getKeyNumber(String keyname) {
		return keyMap.get(keyname);
	}
	
	@SuppressWarnings("unused")
	private static String getKeyName(int keynumber) {
		return keyName[keynumber];
	}
	
	private static boolean isKeyDown(int key) {
		return keysPressed.contains(key);
	}
	

	private static List<List<Integer>> getKeyCodes(String key) {
		List<List<Integer>> keys = new ArrayList<List<Integer>>();
		
		Scanner sc = new Scanner(Constants.getConstant(key));
	    int i = 0;
	    for (String s; (s = sc.findWithinHorizon("(?<=\\{).*?(?=\\})", 0)) != null; i++) {
	    	keys.add(i, new ArrayList<Integer>());
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
		
		return false || ControllerUtils.isButtonPressed(name);
	}
	
	public static void resetKeys() {
		keysPressed.clear();
	}
}
