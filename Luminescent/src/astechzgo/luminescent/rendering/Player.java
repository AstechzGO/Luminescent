package astechzgo.luminescent.rendering;

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
}