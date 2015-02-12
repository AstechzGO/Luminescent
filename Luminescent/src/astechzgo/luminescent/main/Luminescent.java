package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import astechzgo.luminescent.rendering.Player;

public class Luminescent
{
	
	public static Player thePlayer = new Player();
	public static int moveSpeed = 2;
	
	public static void Init()
	{
		
	}
	
	public static void Shutdown()
	{
		
	}
	
	public static void Tick()
	{
		thePlayer.Render();
		
		if(thePlayer.getPosX() < 0)
		{
			thePlayer.setPosX(SCREEN_WIDTH);
		}
		if(thePlayer.getPosX() > SCREEN_WIDTH)
		{
			thePlayer.setPosX(0);
		}
		
		if(thePlayer.getPosY() < 0)
		{
			thePlayer.setPosY(SCREEN_HEIGHT);
		}
		if(thePlayer.getPosY() > SCREEN_HEIGHT)
		{
			thePlayer.setPosY(0);
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			thePlayer.setPosY(thePlayer.getPosY() + moveSpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			thePlayer.setPosY(thePlayer.getPosY() - moveSpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			thePlayer.setPosX(thePlayer.getPosX() + moveSpeed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			thePlayer.setPosX(thePlayer.getPosX() - moveSpeed);
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			moveSpeed = 3;
		}
		else
		{
			moveSpeed = 2;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
		{
			Display.destroy();
			System.exit(0);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F11))
		{
			if(Display.isFullscreen()) {
				setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, false);
			}
			else {
				setDisplayMode(SCREEN_WIDTH, SCREEN_HEIGHT, true);
			}
		}
	}
	
}