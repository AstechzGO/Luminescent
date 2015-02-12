package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

public class Player extends GameRenderedObject
{
	private int posX = 0;
	private int posY = 0;
	
	public void Render()
	{
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		super.RenderCircle(posX, posY, 40);
	}
	
	public int getPosX()
	{
		return posX;
	}
	
	public int getPosY()
	{
		return posY;
	}
	
	public void setPosX(int position)
	{
		posX = position;
	}
	
	public void setPosY(int position)
	{
		posY = position;
	}
	
}