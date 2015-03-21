package astechzgo.luminescent.rendering;

import org.lwjgl.opengl.GL11;

import astechzgo.luminescent.utils.DisplayUtils;

public class Room extends RenderableQuadrilateralGameObject {

	public Room() {
		super(
				
				DisplayUtils.SCREEN_WIDTH / 45, 
				DisplayUtils.SCREEN_HEIGHT / 25, 
				DisplayUtils.SCREEN_WIDTH - (DisplayUtils.SCREEN_WIDTH / 45 * 2),
				DisplayUtils.SCREEN_HEIGHT - (DisplayUtils.SCREEN_HEIGHT / 25 * 2)
				
			);
	}
	
	public void setSize(int width, int height) {
		super.width = width;
		super.height = height;
	}

	@Override
	public void render() {
		GL11.glColor3f(0.15f, 0.15f, 0.15f);
		super.render();
	}
}
