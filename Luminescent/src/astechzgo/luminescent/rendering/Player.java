package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.utils.DisplayUtils;

public class Player extends RenderableCircularGameObject
{
	public Player()
	{
		super(DisplayUtils.SCREEN_WIDTH / 2, DisplayUtils.SCREEN_HEIGHT / 2, 40, 1);	
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
	
	@Override
	public void render()
	{
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		super.render();
	}
}