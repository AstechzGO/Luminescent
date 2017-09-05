package astechzgo.luminescent.utils;

import java.awt.Color;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.rendering.Vulkan.Vertex;
import astechzgo.luminescent.textures.Texture;

public class RenderingUtils {

    /*
	 * A--B
	 * |QD|
	 * D--C
	 */
    @SafeVarargs
    public static void createQuad(WindowCoordinates a, WindowCoordinates b, WindowCoordinates c, WindowCoordinates d, Color color, Texture texture, Supplier<Matrix4f>... matrices) {
        int vAX = ((int) a.getWindowCoordinatesX())
                - ((int) a.getWindowCoordinatesX());
        int vAY = ((int) a.getWindowCoordinatesY())
                - ((int) a.getWindowCoordinatesY());
        int vBX = ((int) b.getWindowCoordinatesX())
                - ((int) a.getWindowCoordinatesX());
        int vBY = ((int) b.getWindowCoordinatesY())
                - ((int) a.getWindowCoordinatesY());
        int vCX = ((int) c.getWindowCoordinatesX())
                - ((int) a.getWindowCoordinatesX());
        int vCY = ((int) c.getWindowCoordinatesY())
                - ((int) a.getWindowCoordinatesY());
        int vDX = ((int) d.getWindowCoordinatesX())
                - ((int) a.getWindowCoordinatesX());
        int vDY = ((int) d.getWindowCoordinatesY())
                - ((int) a.getWindowCoordinatesY());
        
        int tAX = 0;
        int tAY = 0;
        int tBX = 1;
        int tBY = 0;
        int tCX = 1;
        int tCY = 1;
        int tDX = 0;
        int tDY = 1;

        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;
        
        Vertex[] vertices = new Vertex[] {
            new Vertex(new Vector2f(vAX, vAY), new Vector4f(red, green, blue, alpha), new Vector2f(tAX, tAY)),
            new Vertex(new Vector2f(vBX, vBY), new Vector4f(red, green, blue, alpha), new Vector2f(tBX, tBY)),
            new Vertex(new Vector2f(vCX, vCY), new Vector4f(red, green, blue, alpha), new Vector2f(tCX, tCY)),
            new Vertex(new Vector2f(vDX, vDY), new Vector4f(red, green, blue, alpha), new Vector2f(tDX, tDY))
        };
        
        int[] indices = new int[] { 0, 2, 1, 3, 2, 0 };
        
        Vulkan.addObject(vertices, indices, texture, matrices);
    }

	@SafeVarargs
    public static void createCircle(double radius, double pointSeperation, Color color, Texture texture, Supplier<Matrix4f>... matrices) {
		int loops = (int) (360 / pointSeperation);
		
		Vertex[] vertices = new Vertex[loops + 1];

        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;

		for (double angle = 0; angle < 360.0; angle += pointSeperation) {
			double radian = Math.toRadians(angle);

			double xcos = (double) Math.cos(radian);
			double ysin = (float) Math.sin(radian);
			double tempx = xcos * radius;
			double tempy = ysin * radius;
			double tx = Math.cos(Math.toRadians(angle )) * 0.5 + 0.5;
			double ty = Math.sin(Math.toRadians(angle)) * 0.5 + 0.5;
			
			int i = (int) (angle / pointSeperation);
			
			vertices[i] = new Vertex(new Vector2f((float)tempx, (float)tempy), new Vector4f(red, green, blue, alpha), new Vector2f((float)tx, (float)ty));
		}

		// Root of circle
		vertices[loops] = new Vertex(new Vector2f(0, 0), new Vector4f(red, green, blue, alpha), new Vector2f(0.5f, 0.5f));

		int[] indices = new int[loops * 3];

		for (int i = 0; i < loops; i++) {
            indices[i * 3] = i % loops;
            indices[i * 3 + 1] = loops;
            indices[i * 3 + 2] = (i + 1) % loops;
		}
		
		Vulkan.addObject(vertices, indices, texture, matrices);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	@SafeVarargs
    public static void createTextureRegion(WindowCoordinates coordinates, int regX, int regY, int regWidth, int regHeight, Color colour,
			Texture texture, Supplier<Matrix4f>... matrices) {
		/* Vertex positions */
	    WindowCoordinates a = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY());
	    WindowCoordinates b = new WindowCoordinates(coordinates.getWindowCoordinatesX() + regWidth, coordinates.getWindowCoordinatesY());
	    WindowCoordinates c = new WindowCoordinates(coordinates.getWindowCoordinatesX() + regWidth, coordinates.getWindowCoordinatesY() + regHeight);
	    WindowCoordinates d = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + regHeight);

		/* Texture coordinates */
		float tAX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tAY = (float) (regY) / texture.getAsBufferedImage().getHeight();
		float tBX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tBY = (float) (regY) / texture.getAsBufferedImage().getHeight();
		float tCX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tCY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
		float tDX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tDY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();

		createTextureRegion(a, b, c, d, tAX, tAY, tBX, tBY, tCX, tCY, tDX, tDY, colour,
				texture, matrices);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	@SafeVarargs
    public static void createTextureRegion(WindowCoordinates vA, WindowCoordinates vB, WindowCoordinates vC, WindowCoordinates vD,
			float tAX, float tAY, float tBX, float tBY, float tCX, float tCY, float tDX, float tDY, Color colour,
			Texture texture, Supplier<Matrix4f>... matrices) {
		
        int vAX = ((int) vA.getWindowCoordinatesX())
                - ((int) vA.getWindowCoordinatesX());
        int vAY = ((int) vA.getWindowCoordinatesY())
                - ((int) vA.getWindowCoordinatesY());
        int vBX = ((int) vB.getWindowCoordinatesX())
                - ((int) vA.getWindowCoordinatesX());
        int vBY = ((int) vB.getWindowCoordinatesY())
                - ((int) vA.getWindowCoordinatesY());
        int vCX = ((int) vC.getWindowCoordinatesX())
                - ((int) vA.getWindowCoordinatesX());
        int vCY = ((int) vC.getWindowCoordinatesY())
                - ((int) vA.getWindowCoordinatesY());
        int vDX = ((int) vD.getWindowCoordinatesX())
                - ((int) vA.getWindowCoordinatesX());
        int vDY = ((int) vD.getWindowCoordinatesY())
                - ((int) vA.getWindowCoordinatesY());
		
        float red = colour.getRed() / 255.0f;
        float green = colour.getGreen() / 255.0f;
        float blue = colour.getBlue() / 255.0f;
        float alpha = colour.getAlpha() / 255.0f;
        
        Vertex[] vertices = new Vertex[] {
            new Vertex(new Vector2f(vAX, vAY), new Vector4f(red, green, blue, alpha), new Vector2f(tAX, tAY)),
            new Vertex(new Vector2f(vBX, vBY), new Vector4f(red, green, blue, alpha), new Vector2f(tBX, tBY)),
            new Vertex(new Vector2f(vCX, vCY), new Vector4f(red, green, blue, alpha), new Vector2f(tCX, tCY)),
            new Vertex(new Vector2f(vDX, vDY), new Vector4f(red, green, blue, alpha), new Vector2f(tDX, tDY))
        };
        
        int[] indices = new int[] { 0, 2, 1, 3, 2, 0 };
        
        Vulkan.addObject(vertices, indices, texture, matrices);
	}
}