package astechzgo.luminescent.utils;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.Vulkan;
import astechzgo.luminescent.rendering.Vulkan.Vertex;
import astechzgo.luminescent.textures.Texture;

public class RenderingUtils {

    public static void createQuad(WindowCoordinates a, WindowCoordinates b, WindowCoordinates c, WindowCoordinates d, Color color, Texture texture, Supplier<Boolean> doLighting, List<Supplier<Matrix4f>> matrices) {
        createQuad(a, b, c, d, color, texture, doLighting, Optional.empty(), matrices);
    }
    
    /*
	 * A--B
	 * |QD|
	 * D--C
	 */
    public static void createQuad(WindowCoordinates a, WindowCoordinates b, WindowCoordinates c, WindowCoordinates d, Color color, Texture texture, Supplier<Boolean> doLighting, Optional<Supplier<Integer>> currentFrame, List<Supplier<Matrix4f>> matrices) {
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
        
        int count = texture == null ? 1 : texture.count();
        
        float tAX = 0.0f;
        float tAY = 0.0f;
        float tBX = 1.0f / count;
        float tBY = 0.0f;
        float tCX = 1.0f / count;
        float tCY = 1.0f;
        float tDX = 0.0f;
        float tDY = 1.0f;

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
        
        if(currentFrame.isPresent())
            Vulkan.addObject(vertices, indices, texture, doLighting, currentFrame.get(), matrices);
        else {
            Vulkan.addObject(vertices, indices, texture, doLighting, matrices);
        }
    }

    public static void createCircle(double radius, double pointSeperation, Color color, Texture texture, Supplier<Boolean> doLighting, List<Supplier<Matrix4f>> matrices) {
        createCircle(radius, pointSeperation, color, texture, doLighting, Optional.empty(), matrices);
    }
    
    public static void createCircle(double radius, double pointSeperation, Color color, Texture texture, Supplier<Boolean> doLighting, Optional<Supplier<Integer>> currentFrame, List<Supplier<Matrix4f>> matrices) {
		int loops = (int) (360 / pointSeperation);
		
		Vertex[] vertices = new Vertex[loops + 1];

        float red = color.getRed() / 255.0f;
        float green = color.getGreen() / 255.0f;
        float blue = color.getBlue() / 255.0f;
        float alpha = color.getAlpha() / 255.0f;

        int count = texture == null ? 1 : texture.count();
        
		for (double angle = 0; angle < 360.0; angle += pointSeperation) {
			double radian = Math.toRadians(angle);

			double xcos = Math.cos(radian);
			double ysin = (float) Math.sin(radian);
			double tempx = xcos * radius;
			double tempy = ysin * radius;
			double tx = Math.cos(Math.toRadians(angle )) * 0.5 + 0.5;
			double ty = Math.sin(Math.toRadians(angle)) * 0.5 + 0.5;
			
			int i = (int) (angle / pointSeperation);
			
			vertices[i] = new Vertex(new Vector2f((float)tempx, (float)tempy), new Vector4f(red, green, blue, alpha), new Vector2f((float)tx/count, (float)ty));
		}

		// Root of circle
		vertices[loops] = new Vertex(new Vector2f(0, 0), new Vector4f(red, green, blue, alpha), new Vector2f(0.5f/count, 0.5f));

		int[] indices = new int[loops * 3];

		for (int i = 0; i < loops; i++) {
            indices[i * 3] = i % loops;
            indices[i * 3 + 1] = loops;
            indices[i * 3 + 2] = (i + 1) % loops;
		}
		
        if(currentFrame.isPresent())
            Vulkan.addObject(vertices, indices, texture, doLighting, currentFrame.get(), matrices);
        else {
            Vulkan.addObject(vertices, indices, texture, doLighting, matrices);
        }
	}
}