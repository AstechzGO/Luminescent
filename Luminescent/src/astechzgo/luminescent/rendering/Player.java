package astechzgo.luminescent.rendering;

import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;

import astechzgo.luminescent.utils.DisplayUtils;

public class Player extends RenderableCircularGameObject
{
	public Player()
	{
		super(1920 / 2, 1080 / 2, 40, 1);
	}
	
	public double getPosX()
	{
		return super.x;
	}
	
	public double getPosY()
	{
		return super.y;
	}
	
	public void setPosX(double position)
	{
		super.x = position;
	}
	
	public void setPosY(double position)
	{
		super.y = position;
	}
	
	public void setRadius(double radius)
	{
		this.radius = radius;
	}
	
	public double getRadius()
	{
		return radius;
	}
	
	@Override
	public void resize() {
		scaledX = ((int) Math
				.round((double) (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset)) / 2)
				+ DisplayUtils.widthOffset;
		scaledY = ((int) Math
				.round((double) (DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset)) / 2)
				+ DisplayUtils.heightOffset;
				
		scaledRadius = (int) Math
				.round((double) radius / 1920 * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));

		oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
		oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
	}
	
	public double setRotation() {		
		/*if(x == 0 && y == 0)
			return;
		else if(x == 0 && y == -1)
			rotation = 90;
		else if(x == 0 && y == 1)
			rotation = 270;
		else if(x == -1 && y == -1)
			rotation = 135;
		else if(x == -1 && y == 0)
			rotation = 180;
		else if(x == -1 && y == 1)
			rotation = 225;
		else if(x == 1 && y == -1)
			rotation = 45;
		else if(x == 1 && y == 0)
			rotation = 0;
		else if(x == 1 && y == 1)
			rotation = 315;
		else
			rotation = 0;*/
		DoubleBuffer mxpos = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer mypos = BufferUtils.createDoubleBuffer(1);
		
		GLFW.glfwGetCursorPos(DisplayUtils.getHandle(), mxpos, mypos);
		
		double x = mxpos.get(0);
		double y = mypos.get(0);
		
		mxpos.clear();
		mypos.clear();
		
		IntBuffer xpos = BufferUtils.createIntBuffer(1);
		IntBuffer ypos = BufferUtils.createIntBuffer(1);
		
		
		GLFW.glfwGetWindowPos(DisplayUtils.getHandle(), xpos, ypos);
		
		x = x + xpos.get(0);
		y = -y + GLFW.glfwGetVideoMode(DisplayUtils.monitor).height()- ypos.get(0);
		
		xpos.clear();
		ypos.clear();
		
		double scaledX = x / GLFW.glfwGetVideoMode(DisplayUtils.monitor).width() * 1920;
		double scaledY = y / GLFW.glfwGetVideoMode(DisplayUtils.monitor).height() * 1080;
		
		double m = (1080 / 2 - scaledY) / (1920 / 2 - scaledX);
		if(scaledX <= 1920 / 2)
			rotation =  360 - Math.toDegrees(Math.atan(m)+180);
		else
			rotation = 360 - Math.toDegrees(Math.atan(m));
		return rotation;
	}
}