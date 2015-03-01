package astechzgo.luminescent.main;

import static astechzgo.luminescent.utils.DisplayUtils.SCREEN_HEIGHT;
import static astechzgo.luminescent.utils.DisplayUtils.SCREEN_WIDTH;
import static astechzgo.luminescent.utils.DisplayUtils.setDisplayMode;

import java.awt.Toolkit;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.rendering.Player;
import astechzgo.luminescent.sound.SoundList;
import astechzgo.luminescent.sound.SoundManager;
import astechzgo.luminescent.utils.DisplayUtils;
import astechzgo.luminescent.utils.RenderingUtils;

public class Luminescent
{
	
	public static Player thePlayer = new Player();
	public static double moveSpeed = 0.5;
	
	public static long lastMove = System.currentTimeMillis();
	
	public static void Init()
	{
		try {
			Cursor emptyCursor = new Cursor(1, 1, 0, 0, 1, BufferUtils.createIntBuffer(1), null);
			Mouse.setNativeCursor(emptyCursor);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		SoundManager manager = new SoundManager();
		SoundList.initSoundList(manager);
	}
	
	public static void Shutdown()
	{
		Display.destroy();
		System.exit(0);
	}
	
	public static void Tick()
	{
		thePlayer.render();
		
		GL11.glColor3f(0.0f, 0.4f, 0.6f);
		RenderingUtils.RenderQuad(0, 0, SCREEN_WIDTH / 45, SCREEN_HEIGHT);
		RenderingUtils.RenderQuad(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT / 25);
		RenderingUtils.RenderQuad(SCREEN_WIDTH - (SCREEN_WIDTH / 45), 0, SCREEN_WIDTH / 45, SCREEN_HEIGHT);
		RenderingUtils.RenderQuad(0, SCREEN_HEIGHT - (SCREEN_HEIGHT / 25), SCREEN_WIDTH, SCREEN_HEIGHT / 25);
		
		int multiplier = (int) (System.currentTimeMillis() - lastMove);
		lastMove = System.currentTimeMillis();
		
		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
		{
			Luminescent.moveSpeed = 0.88;
		}
		else
		{
			Luminescent.moveSpeed = 0.5;
		}
		
		double speed = Luminescent.moveSpeed * multiplier;
		
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
		
		if(Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
		{
			thePlayer.setPosY(thePlayer.getPosY() + speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
		{
			thePlayer.setPosY(thePlayer.getPosY() - speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_D) || Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
		{
			thePlayer.setPosX(thePlayer.getPosX() + speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_A) || Keyboard.isKeyDown(Keyboard.KEY_LEFT))
		{
			thePlayer.setPosX(thePlayer.getPosX() - speed);
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE) && Keyboard.isKeyDown(Keyboard.KEY_TAB))
		{
			Shutdown();
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F11))
		{
			if(Display.isFullscreen()) {
				setDisplayMode(854, 480, false);
			}
			else {
				setDisplayMode((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth(),
						(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight(), true);
			}
		}
		if(Keyboard.isKeyDown(Keyboard.KEY_F2))
		{
			File dir = new File("screenshots");
			if(!dir.exists() || !dir.isDirectory()) {
				dir.mkdir();
			}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
			Date dt = new Date();
			String S = sdf.format(dt);

			try {
				DisplayUtils.takeScreenshot(new File("screenshots/" + S + ".png"));
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
	}
	
}