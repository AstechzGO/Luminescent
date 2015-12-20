package astechzgo.luminescent.rendering;

public class Camera {
	
	private static int x = 1920 / 2;
	private static int y = 1080 / 2;
	
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
