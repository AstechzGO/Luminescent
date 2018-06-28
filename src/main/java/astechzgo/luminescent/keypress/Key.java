package astechzgo.luminescent.keypress;

import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.KeyboardUtils;

public enum Key {

	KEYS_MOVEMENT_FASTER(Constants.KEYS_MOVEMENT_FASTER),
	KEYS_MOVEMENT_UP(Constants.KEYS_MOVEMENT_UP),
	KEYS_MOVEMENT_LEFT(Constants.KEYS_MOVEMENT_LEFT),
	KEYS_MOVEMENT_DOWN(Constants.KEYS_MOVEMENT_DOWN),
	KEYS_MOVEMENT_RIGHT(Constants.KEYS_MOVEMENT_RIGHT),
	KEYS_ACTION_SHOOT(Constants.KEYS_ACTION_SHOOT),
	KEYS_UTIL_SCREENSHOT(Constants.KEYS_UTIL_SCREENSHOT),
	KEYS_UTIL_FULLSCREEN(Constants.KEYS_UTIL_FULLSCREEN),
	KEYS_UTIL_EXIT(Constants.KEYS_UTIL_EXIT),
	KEYS_UTIL_NEXTWINDOW(Constants.KEYS_UTIL_NEXTWINDOW),
	KEYS_UTIL_TOGGLELIGHTING(Constants.KEYS_UTIL_TOGGLELIGHTING);
	
	public static void updateKeys() {
		for(Key key : Key.values())
			key.update();
	}
	
	private final String keyName;
	
	private boolean isDown;
	private boolean wasDown;
	
	Key(String keyName) {
		this.keyName = keyName;
	}
	
	public void update() {
		wasDown = isDown;
		isDown = KeyboardUtils.isKeyDown(keyName);
	}
	
	public boolean isKeyDown() {
		return isDown;
	}
	
	public boolean isKeyDownOnce() {
		return !wasDown && isDown;
	}
}
