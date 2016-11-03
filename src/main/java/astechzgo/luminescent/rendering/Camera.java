package astechzgo.luminescent.rendering;

import astechzgo.luminescent.coordinates.GameCoordinates;

public class Camera {
	
	public static final double CAMERA_WIDTH = 1920;
	public static final double CAMERA_HEIGHT = 1080;
	
	private static GameCoordinates cameraCoordinates = new GameCoordinates(CAMERA_WIDTH / 2, CAMERA_HEIGHT / 2);
	
	public static GameCoordinates getCameraCoordinates() {
		return cameraCoordinates;
	}
	
	public static void setCameraCoordinates(GameCoordinates cameraCoordinates) {
		Camera.cameraCoordinates = cameraCoordinates;
	}
}
