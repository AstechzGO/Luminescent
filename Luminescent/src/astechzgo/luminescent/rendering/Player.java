package astechzgo.luminescent.rendering;

import astechzgo.luminescent.utils.DisplayUtils;

public class Player extends RenderableCircularGameObject
{
	public Player()
	{
		super(DisplayUtils.monitorWidth / 2, DisplayUtils.monitorHeight / 2, (int)Math.round(0.02083333333 * DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2), 1);
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
		super.render();
	}
}