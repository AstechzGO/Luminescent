package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.awt.Toolkit;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

import astechzgo.luminescent.rendering.Player;

public class Luminescent
{
	
	public static Player thePlayer = new Player();
	public static int playerX = 500;
	public static int playerY = 500;
	public static int moveSpeed = 2;
	
	public static void Init()
	{
		
	}
	
	public static void Shutdown()
	{
		
	}
	
	public static void Tick()
	{
		thePlayer.Render(playerX, playerY);
		
		if(playerX < 0)
		{
			playerX = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		}
		if(playerX > (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth())
		{
			playerX = 0;
		}
		
		if(playerY < 0)
		{
			playerY = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		}
		if(playerY > (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight())
		{
			playerY = 0;
		}
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W))
		{
			playerY += moveSpeed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S))
		{
			playerY -= moveSpeed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D))
		{
			playerX += moveSpeed;
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A))
		{
			playerX -= moveSpeed;
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
				setDisplayMode(800, 480, false);
			}
			else {
				setDisplayMode((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(), 
						(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(), true);
			}
		}
	}
	
}