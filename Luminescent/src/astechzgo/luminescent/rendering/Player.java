package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

public class Player extends GameRenderedObject
{
	
	public void Render(int x, int y)
	{
		GL11.glColor3f(0.0f, 0.0f, 0.0f);
		super.RenderCircle(x, y, 40);
	}
	
}