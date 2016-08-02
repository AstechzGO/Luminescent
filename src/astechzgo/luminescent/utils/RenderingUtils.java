package astechzgo.luminescent.utils;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.textures.Texture;

public class RenderingUtils {

	/*
	 * A--B
	 * |QD|
	 * D--C
	 */
	public static void RenderQuad(ScaledWindowCoordinates a, ScaledWindowCoordinates b, ScaledWindowCoordinates c, ScaledWindowCoordinates d) {
		int aX = (int) a.getScaledWindowCoordinatesX();
		int aY = (int) a.getScaledWindowCoordinatesY();
		int bX = (int) b.getScaledWindowCoordinatesX();
		int bY = (int) b.getScaledWindowCoordinatesY();
		int cX = (int) c.getScaledWindowCoordinatesX();
		int cY = (int) c.getScaledWindowCoordinatesY();
		int dX = (int) d.getScaledWindowCoordinatesX();
		int dY = (int) d.getScaledWindowCoordinatesY();
		
		// Vertices, the order is not important. XYZW instead of XYZ
		float[] vertices = {
				aX, aY, 0f, 1f,
				bX, bY, 0f, 1f,
				cX, cY, 0f, 1f,
				dX, dY, 0f, 1f 
		};
		
		FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer currentColour = MemoryUtil.memAllocFloat(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float red = currentColour.get();
		float green = currentColour.get();
		float blue = currentColour.get();
		float alpha = currentColour.get();

		currentColour.clear();

		float[] colors = { 
				red, green, blue, alpha,
				red, green, blue, alpha,
				red, green, blue, alpha,
				red, green, blue, alpha
		};	
		FloatBuffer colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		float[] textureCoords = {
				0, 1,
				1, 1,
				1, 0,
				0, 0
		};
		FloatBuffer textureCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();

		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};
		int indicesCount = indices.length;
		ByteBuffer indicesBuffer = MemoryUtil.memAlloc(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();

		// Create a new Vertex Array Object in memory and select it (bind)
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind) -
		// VERTICES
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - COLORS
		int vbocId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the texture coords and select it (bind) -
		// TEXTURES
		int vbotId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		int vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		// Bind to the index VBO that has all the information about the order of
		// the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		
		MemoryUtil.memFree(verticesBuffer);
		MemoryUtil.memFree(currentColour);
		MemoryUtil.memFree(colorsBuffer);
		MemoryUtil.memFree(textureCoordsBuffer);
		MemoryUtil.memFree(indicesBuffer);
	}

	public static void RenderQuad(ScaledWindowCoordinates a, ScaledWindowCoordinates b, ScaledWindowCoordinates c, ScaledWindowCoordinates d, Texture texture) {
		if (texture.getAsTexture() != 0) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			GL11.glColor3f(1, 1, 1);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
			RenderQuad(a, b, c, d);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
			
			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}

	public static void RenderCircle(ScaledWindowCoordinates coordinates, double radius, double pointSeperation, double rotation) {
		int loops = (int) (360 / pointSeperation);
		
		// Add vertex for root of circle
		float[] vertices = new float[(loops + 1) * 4];
		float[] colors = new float[(loops + 1) * 4];
		float[] texCoords = new float[(loops + 1) * 2];

		FloatBuffer currentColour = MemoryUtil.memAllocFloat(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float red = currentColour.get();
		float green = currentColour.get();
		float blue = currentColour.get();
		float alpha = currentColour.get();

		currentColour.clear();

		for (double angle = 0; angle < 360.0; angle += pointSeperation) {
			double radian = Math.toRadians(angle);

			double xcos = (double) Math.cos(radian);
			double ysin = (float) Math.sin(radian);
			double tempx = xcos * radius + coordinates.getScaledWindowCoordinatesX();
			double tempy = ysin * radius + coordinates.getScaledWindowCoordinatesY();
			double tx = Math.cos(Math.toRadians(angle + rotation)) * 0.5 + 0.5;
			double ty = Math.sin(Math.toRadians(angle + rotation)) * 0.5 + 0.5;

			// GL11.glTexCoord2d(tx, ty);
			// GL11.glVertex2d(tempx, tempy);
			int i = (int) (angle / pointSeperation);
			texCoords[i * 2] = (float) tx;
			texCoords[i * 2 + 1] = (float) ty;

			vertices[i * 4] = (float) tempx;
			vertices[i * 4 + 1] = (float) tempy;
			vertices[i * 4 + 2] = 0.0f;
			vertices[i * 4 + 3] = 1.0f;

			colors[i * 4] = red;
			colors[i * 4 + 1] = green;
			colors[i * 4 + 2] = blue;
			colors[i * 4 + 3] = alpha;
		}

		// Root of circle
		vertices[loops * 4] = (int) coordinates.getScaledWindowCoordinatesX();
		vertices[loops * 4 + 1] = (int) coordinates.getScaledWindowCoordinatesY();
		vertices[loops * 4 + 2] = 0.0f;
		vertices[loops * 4 + 3] = 1.0f;

		texCoords[loops * 2] = 0.5f;
		texCoords[loops * 2 + 1] = 0.5f;

		colors[loops * 4] = red;
		colors[loops * 4 + 1] = green;
		colors[loops * 4 + 2] = blue;
		colors[loops * 4 + 3] = alpha;

		// Vertices, the order is not important. XYZW instead of XYZ
		FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		FloatBuffer textureCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
		textureCoordsBuffer.put(texCoords);
		textureCoordsBuffer.flip();

		int[] indices = new int[loops * 3];

		for (int i = 0; i < loops; i++) {
			indices[i * 3] = loops;
			indices[i * 3 + 1] = i % loops;
			indices[i * 3 + 2] = (i + 1) % loops;
		}

		int indicesCount = indices.length;
		IntBuffer indicesBuffer = MemoryUtil.memAllocInt(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();

		// Create a new Vertex Array Object in memory and select it (bind)
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind) -
		// VERTICES
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - COLORS
		int vbocId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the texture coords and select it (bind) -
		// TEXTURES
		int vbotId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		int vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		// Bind to the index VBO that has all the information about the order of
		// the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_INT, 0);

		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		
		MemoryUtil.memFree(verticesBuffer);
		MemoryUtil.memFree(currentColour);
		MemoryUtil.memFree(colorsBuffer);
		MemoryUtil.memFree(textureCoordsBuffer);
		MemoryUtil.memFree(indicesBuffer);
	}

	public static void RenderCircle(ScaledWindowCoordinates coordinates, double radius, double pointSeperation, double rotation,
			Texture texture) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
		RenderCircle(coordinates, radius, pointSeperation, rotation);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	public static void DrawTextureRegion(ScaledWindowCoordinates coordinates, int regX, int regY, int regWidth, int regHeight, Color colour,
			Texture texture) {
		/* Vertex positions */
		ScaledWindowCoordinates a = new ScaledWindowCoordinates(coordinates.getScaledWindowCoordinatesX(), coordinates.getScaledWindowCoordinatesY() + regHeight);
		ScaledWindowCoordinates b = new ScaledWindowCoordinates(coordinates.getScaledWindowCoordinatesX() + regWidth, coordinates.getScaledWindowCoordinatesY() + regHeight);
		ScaledWindowCoordinates c = new ScaledWindowCoordinates(coordinates.getScaledWindowCoordinatesX() + regWidth, coordinates.getScaledWindowCoordinatesY());
		ScaledWindowCoordinates d = new ScaledWindowCoordinates(coordinates.getScaledWindowCoordinatesX(), coordinates.getScaledWindowCoordinatesY());

		/* Texture coordinates */
		float tAX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tAY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
		float tBX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tBY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
		float tCX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tCY = (float) (regY) / texture.getAsBufferedImage().getHeight();
		float tDX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tDY = (float) (regY) / texture.getAsBufferedImage().getHeight();

		DrawTextureRegion(a, b, c, d, tAX, tAY, tBX, tBY, tCX, tCY, tDX, tDY, colour,
				texture);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	public static void DrawTextureRegion(ScaledWindowCoordinates vA, ScaledWindowCoordinates vB, ScaledWindowCoordinates vC, ScaledWindowCoordinates vD,
			float tAX, float tAY, float tBX, float tBY, float tCX, float tCY, float tDX, float tDY, Color colour,
			Texture texture) {
		
		int vAX = (int) vA.getScaledWindowCoordinatesX();
		int vAY = (int) vA.getScaledWindowCoordinatesY();
		int vBX = (int) vB.getScaledWindowCoordinatesX();
		int vBY = (int) vB.getScaledWindowCoordinatesY();
		int vCX = (int) vC.getScaledWindowCoordinatesX();
		int vCY = (int) vC.getScaledWindowCoordinatesY();
		int vDX = (int) vD.getScaledWindowCoordinatesX();
		int vDY = (int) vD.getScaledWindowCoordinatesY();

		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glColor4f((float) colour.getRed() / 255, (float) colour.getGreen() / 255,
				(float) colour.getBlue() / 255, (float) colour.getAlpha() / 255);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());

		// Vertices, the order is not important. XYZW instead of XYZ
		float[] vertices = {
				vAX, vAY, 0f, 1f,
				vBX, vBY, 0f, 1f,
				vCX, vCY, 0f, 1f,
				vDX, vDY, 0f, 1f
		};
		FloatBuffer verticesBuffer = MemoryUtil.memAllocFloat(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer currentColour = MemoryUtil.memAllocFloat(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float red = currentColour.get();
		float green = currentColour.get();
		float blue = currentColour.get();
		float alpha = currentColour.get();

		currentColour.clear();

		float[] colors = {
				red, green, blue, alpha,
				red, green, blue, alpha,
				red, green, blue, alpha,
				red, green, blue, alpha
		};
		FloatBuffer colorsBuffer = MemoryUtil.memAllocFloat(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		float[] textureCoords = {
				tAX, tAY,
				tBX, tBY,
				tCX, tCY,
				tDX, tDY
		};
		FloatBuffer textureCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();

		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};
		int indicesCount = indices.length;
		ByteBuffer indicesBuffer = MemoryUtil.memAlloc(indicesCount);
		indicesBuffer.put(indices);
		indicesBuffer.flip();

		// Create a new Vertex Array Object in memory and select it (bind)
		int vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);

		// Create a new Vertex Buffer Object in memory and select it (bind) -
		// VERTICES
		int vboId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, verticesBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(0, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the indices and select it (bind) - COLORS
		int vbocId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbocId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, colorsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(1, 4, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Create a new VBO for the texture coords and select it (bind) -
		// TEXTURES
		int vbotId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbotId);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, textureCoordsBuffer, GL15.GL_STATIC_DRAW);
		GL20.glVertexAttribPointer(2, 2, GL11.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		// Deselect (bind to 0) the VAO
		GL30.glBindVertexArray(0);

		// Create a new VBO for the indices and select it (bind) - INDICES
		int vboiId = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_STATIC_DRAW);
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

		// Bind to the VAO that has all the information about the vertices
		GL30.glBindVertexArray(vaoId);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(2);

		// Bind to the index VBO that has all the information about the order of
		// the vertices
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboiId);

		// Draw the vertices
		GL11.glDrawElements(GL11.GL_TRIANGLES, indicesCount, GL11.GL_UNSIGNED_BYTE, 0);

		// Put everything back to default (deselect)
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(2);
		GL30.glBindVertexArray(0);
		
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		
		MemoryUtil.memFree(verticesBuffer);
		MemoryUtil.memFree(currentColour);
		MemoryUtil.memFree(colorsBuffer);
		MemoryUtil.memFree(textureCoordsBuffer);
		MemoryUtil.memFree(indicesBuffer);
	}
}