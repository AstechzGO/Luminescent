package astechzgo.luminescent.rendering;

public class Camera {
	
	public static final double CAMERA_WIDTH = 1920;
	public static final double CAMERA_HEIGHT = 1080;
	
	private static double x = CAMERA_WIDTH / 2;
	private static double y = CAMERA_HEIGHT / 2;
	
	public static double getX() {
		return x;
	}
	
	public static double getY() {
		return y;
	}
	
	public static void setX(double x) {
		Camera.x = x;
	}
	
	public static void setY(double y) {
		Camera.y = y;
	}
}
