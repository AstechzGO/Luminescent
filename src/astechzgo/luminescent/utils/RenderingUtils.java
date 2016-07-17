package astechzgo.luminescent.utils;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import astechzgo.luminescent.textures.Texture;

public class RenderingUtils {

	/*
	 * A--B
	 * |QD|
	 * D--C
	 */
	public static void RenderQuad(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY) {
		// Vertices, the order is not important. XYZW instead of XYZ
		float[] vertices = {
				aX, aY, 0f, 1f,
				bX, bY, 0f, 1f,
				cX, cY, 0f, 1f,
				dX, dY, 0f, 1f 
		};
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer currentColour = BufferUtils.createFloatBuffer(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float r = currentColour.get();
		float g = currentColour.get();
		float b = currentColour.get();
		float a = currentColour.get();

		currentColour.clear();

		float[] colors = { 
				r, g, b, a,
				r, g, b, a,
				r, g, b, a,
				r, g, b, a
		};
		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		float[] textureCoords = {
				0, 1,
				1, 1,
				1, 0,
				0, 0
		};
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();

		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};
		int indicesCount = indices.length;
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
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
	}

	public static void RenderQuad(int aX, int aY, int bX, int bY, int cX, int cY, int dX, int dY, Texture texture) {
		if (texture.getAsTexture() != 0) {
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			GL11.glColor3f(1, 1, 1);

			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
			RenderQuad(aX, aY, bX, bY, cX, cY, dX, dY);

			GL11.glDisable(GL11.GL_TEXTURE_2D);
		}
	}

	public static void RenderCircle(int x, int y, double radius, double pointSeperation, double rotation) {
		int loops = (int) (360 / pointSeperation);

		// Add vertex for root of circle
		float[] vertices = new float[(loops + 1) * 4];
		float[] colors = new float[(loops + 1) * 4];
		float[] texCoords = new float[(loops + 1) * 2];

		FloatBuffer currentColour = BufferUtils.createFloatBuffer(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float r = currentColour.get();
		float g = currentColour.get();
		float b = currentColour.get();
		float a = currentColour.get();

		currentColour.clear();

		for (double angle = 0; angle < 360.0; angle += pointSeperation) {
			double radian = Math.toRadians(angle);

			double xcos = (double) Math.cos(radian);
			double ysin = (float) Math.sin(radian);
			double tempx = xcos * radius + x;
			double tempy = ysin * radius + y;
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

			colors[i * 4] = r;
			colors[i * 4 + 1] = g;
			colors[i * 4 + 2] = b;
			colors[i * 4 + 3] = a;
		}

		// Root of circle
		vertices[loops * 4] = x;
		vertices[loops * 4 + 1] = y;
		vertices[loops * 4 + 2] = 0.0f;
		vertices[loops * 4 + 3] = 1.0f;

		texCoords[loops * 2] = 0.5f;
		texCoords[loops * 2 + 1] = 0.5f;

		colors[loops * 4] = r;
		colors[loops * 4 + 1] = g;
		colors[loops * 4 + 2] = b;
		colors[loops * 4 + 3] = a;

		// Vertices, the order is not important. XYZW instead of XYZ
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(texCoords.length);
		textureCoordsBuffer.put(texCoords);
		textureCoordsBuffer.flip();

		int[] indices = new int[loops * 3];

		for (int i = 0; i < loops; i++) {
			indices[i * 3] = loops;
			indices[i * 3 + 1] = i % loops;
			indices[i * 3 + 2] = (i + 1) % loops;
		}

		int indicesCount = indices.length;
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indicesCount);
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
	}

	public static void RenderCircle(int x, int y, double radius, double pointSeperation, double rotation,
			Texture texture) {
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glColor3f(1, 1, 1);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());
		RenderCircle(x, y, radius, pointSeperation, rotation);

		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	public static void DrawTextureRegion(int x, int y, int regX, int regY, int regWidth, int regHeight, Color colour,
			Texture texture) {
		/* Vertex positions */
		int vAX = x;
		int vAY = y + regHeight;
		int vBX = x + regWidth;
		int vBY = y + regHeight;
		int vCX = x + regWidth;
		int vCY = y;
		int vDX = x;
		int vDY = y;

		/* Texture coordinates */
		float tAX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tAY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
		float tBX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tBY = (float) (regY + regHeight) / texture.getAsBufferedImage().getHeight();
		float tCX = (float) (regX + regWidth) / texture.getAsBufferedImage().getWidth();
		float tCY = (float) (regY) / texture.getAsBufferedImage().getHeight();
		float tDX = (float) (regX) / texture.getAsBufferedImage().getWidth();
		float tDY = (float) (regY) / texture.getAsBufferedImage().getHeight();

		DrawTextureRegion(vAX, vAY, vBX, vBY, vCX, vCY, vDX, vDY, tAX, tAY, tBX, tBY, tCX, tCY, tDX, tDY, colour,
				texture);
	}

	/**
	 * Draws a texture region with the currently bound texture on specified
	 * coordinates.
	 */
	public static void DrawTextureRegion(int vAX, int vAY, int vBX, int vBY, int vCX, int vCY, int vDX, int vDY,
			float tAX, float tAY, float tBX, float tBY, float tCX, float tCY, float tDX, float tDY, Color colour,
			Texture texture) {
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		GL11.glColor4f((float) colour.getRed() / 255, (float) colour.getGreen() / 255,
				(float) colour.getBlue() / 256, (float) colour.getAlpha() / 255);

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getAsTexture());

		// Vertices, the order is not important. XYZW instead of XYZ
		float[] vertices = {
				vAX, vAY, 0f, 1f,
				vBX, vBY, 0f, 1f,
				vCX, vCY, 0f, 1f,
				vDX, vDY, 0f, 1f
		};
		FloatBuffer verticesBuffer = BufferUtils.createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices);
		verticesBuffer.flip();

		FloatBuffer currentColour = BufferUtils.createFloatBuffer(4);
		GL11.glGetFloatv(GL11.GL_CURRENT_COLOR, currentColour);

		float r = currentColour.get();
		float g = currentColour.get();
		float b = currentColour.get();
		float a = currentColour.get();

		currentColour.clear();

		float[] colors = {
				r, g, b, a,
				r, g, b, a,
				r, g, b, a,
				r, g, b, a
		};
		FloatBuffer colorsBuffer = BufferUtils.createFloatBuffer(colors.length);
		colorsBuffer.put(colors);
		colorsBuffer.flip();

		float[] textureCoords = {
				tAX, tAY,
				tBX, tBY,
				tCX, tCY,
				tDX, tDY
		};
		FloatBuffer textureCoordsBuffer = BufferUtils.createFloatBuffer(textureCoords.length);
		textureCoordsBuffer.put(textureCoords);
		textureCoordsBuffer.flip();

		// OpenGL expects to draw vertices in counter clockwise order by default
		byte[] indices = {
				0, 1, 2,
				2, 3, 0
		};
		int indicesCount = indices.length;
		ByteBuffer indicesBuffer = BufferUtils.createByteBuffer(indicesCount);
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
		
		GL11.glDisable(GL11.GL_TEXTURE_2D);
	}
}