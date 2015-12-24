package astechzgo.luminescent.rendering;

public class Camera {
	
	public static final int CAMERA_WIDTH = 1920;
	public static final int CAMERA_HEIGHT = 1080;
	
	private static int x = CAMERA_WIDTH / 2;
	private static int y = CAMERA_HEIGHT / 2;
	
	public static int getX() {
		return x;
	}
	
	public static int getY() {
		return y;
	}
	
	public static void setX(int x) {
		Camera.x = x;
	}
	
	public static void setY(int y) {
		Camera.y = y;
	}
}
