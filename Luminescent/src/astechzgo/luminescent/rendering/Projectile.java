package astechzgo.luminescent.rendering;

import java.awt.Color;

import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.main.Luminescent;

public class Projectile extends RenderableRectangularGameObject {
	
	private double lastDelta;
	private double rotation;
	
	public Projectile(int x, int y) {
		super(x, y, 5,5);
		super.setColour(new Color(0.5f, 0.6f, 0.2f));
		lastDelta = GLFW.glfwGetTime() * 1000;
		rotation = Luminescent.thePlayer.setRotation();
	}
	
	public void fireBullet() {
		double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
		super.x = (int) (Luminescent.thePlayer.getPosX() + 1 * delta* Math.cos(Math.toRadians(-rotation)));
		super.y = (int) (Luminescent.thePlayer.getPosY() + 1 * delta *Math.sin(Math.toRadians(-rotation)));
	}
	
	public double getX() {
		return super.x;
	}

	public double getY() {
		return super.y;
	}
}
