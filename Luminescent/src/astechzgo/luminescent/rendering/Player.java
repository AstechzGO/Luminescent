package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.utils.DisplayUtils;

public class Player extends GameRenderedObject
{
	private double posX = DisplayUtils.SCREEN_WIDTH / 2;
	private double posY = DisplayUtils.SCREEN_HEIGHT / 2;
	
	public void Render()
	{
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		super.RenderCircle(posX, posY, 40);
	}
	
	public double getPosX()
	{
		return posX;
	}
	
	public double getPosY()
	{
		return posY;
	}
	
	public void setPosX(double position)
	{
		posX = position;
	}
	
	public void setPosY(double position)
	{
		posY = position;
	}
	
}