package astechzgo.luminescent.keypress;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;
import static astechzgo.luminescent.utils.SystemUtils.newFile;
import static astechzgo.luminescent.keypress.Key.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.main.Luminescent;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.KeyboardUtils;

public class KeyPressUtils {
	
	public static void checkUtils() {
	
		if(KEYS_UTIL_EXIT.isKeyDownOnce()) {
			Luminescent.Shutdown();
		}
		if(KEYS_UTIL_FULLSCREEN.isKeyDownOnce()) {
			if(DisplayUtils.isFullscreen()) {
				setDisplayMode(848, 477, false);
				KeyboardUtils.resetKeys();
			}
			else {
				setDisplayMode(DisplayUtils.vidmode.width(),
					DisplayUtils.vidmode.height(), true);
				KeyboardUtils.resetKeys();
			}
		}
		if(KEYS_UTIL_SCREENSHOT.isKeyDownOnce()) {			
			File dir = newFile("screenshots/");
			
			if(!dir.exists() || !dir.isDirectory()) {
				dir.mkdir();
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			Date dt = new Date();
			String S = sdf.format(dt);
			
			try {
				DisplayUtils.takeScreenshot(newFile("screenshots/" + S + ".png"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			if(Luminescent.SOUND_MANAGER.getSoundSystem().playing("keys.util.screenshot.CameraClick"))
				Luminescent.SOUND_MANAGER.getSoundSystem().stop("keys.util.screenshot.CameraClick");
			
			Luminescent.SOUND_MANAGER.getSoundSystem().play("keys.util.screenshot.CameraClick");
		}
		if(KEYS_UTIL_NEXTWINDOW.isKeyDownOnce()) {
			if(GLFW.glfwGetMonitors().capacity() > 1) {
				if(DisplayUtils.isFullscreen()) {
					setDisplayMode(848, 477, false);
					DisplayUtils.nextMonitor();
					setDisplayMode(DisplayUtils.monitorWidth, DisplayUtils.monitorHeight, true);
				}
				else {
					DisplayUtils.nextMonitor();
				}
			}
		}
	}	
}

