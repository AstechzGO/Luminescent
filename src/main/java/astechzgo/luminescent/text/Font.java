/*
 * The MIT License (MIT)
 *
 * Copyright © 2015, Heiko Brumme
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package astechzgo.luminescent.text;

import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;
import static java.awt.Font.TRUETYPE_FONT;

import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.RectangularObjectRenderer;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.RenderingUtils;

/**
 * This class contains a font texture for drawing text.
 *
 * @author Heiko Brumme
 */
public class Font {
	
	public static final String TEXTURE_NAME = "Glyph-Atlas";
	
	public static final Font NORMAL_FONT = new Font();

    /**
     * Contains the glyphs for each char.
     */
    private final Map<Character, Glyph> glyphs;
    /**
     * Contains the font texture.
     */
    private final CharTexture texture;

    /**
     * Height of the font.
     */
    private int fontHeight;

    private final java.awt.Font font;
    
    /**
     * Creates a default antialiased font with monospaced glyphs and default
     * size 16.
     */
    public Font() {
        this(new java.awt.Font(MONOSPACED, PLAIN, 16), true);
    }

    /**
     * Creates a default font with monospaced glyphs and default size 16.
     *
     * @param antiAlias Wheter the font should be antialiased or not
     */
    public Font(boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, 16), antiAlias);
    }

    /**
     * Creates a default antialiased font with monospaced glyphs and specified
     * size.
     *
     * @param size Font size
     */
    public Font(int size) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), true);
    }

    /**
     * Creates a default font with monospaced glyphs and specified size.
     *
     * @param size      Font size
     * @param antiAlias Wheter the font should be antialiased or not
     */
    public Font(int size, boolean antiAlias) {
        this(new java.awt.Font(MONOSPACED, PLAIN, size), antiAlias);
    }

    /**
     * Creates a antialiased Font from an input stream.
     *
     * @param in   The input stream
     * @param size Font size
     *
     * @throws FontFormatException if fontFile does not contain the required
     *                             font tables for the specified format
     * @throws IOException         If font can't be read
     */
    public Font(InputStream in, int size) throws FontFormatException, IOException {
        this(in, size, true);
    }

    /**
     * Creates a Font from an input stream.
     *
     * @param in        The input stream
     * @param size      Font size
     * @param antiAlias Wheter the font should be antialiased or not
     *
     * @throws FontFormatException if fontFile does not contain the required
     *                             font tables for the specified format
     * @throws IOException         If font can't be read
     */
    public Font(InputStream in, int size, boolean antiAlias) throws FontFormatException, IOException {
        this(java.awt.Font.createFont(TRUETYPE_FONT, in).deriveFont(PLAIN, size), antiAlias);
    }

    /**
     * Creates a antialiased font from an AWT Font.
     *
     * @param font The AWT Font
     */
    public Font(java.awt.Font font) {
        this(font, true);
    }

    /**
     * Creates a font from an AWT Font.
     *
     * @param font      The AWT Font
     * @param antiAlias Wheter the font should be antialiased or not
     */
    public Font(java.awt.Font font, boolean antiAlias) {
    	if(font.getSize() == 0)
    		font = font.deriveFont(1.0f);
    		
    	FontRenderContext frc = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    	if(font.getStringBounds("i", frc).getWidth() != font.getStringBounds("m", frc).getWidth()) {
    	    this.font = new java.awt.Font(MONOSPACED, PLAIN, font.getSize());
    	}
    	else {
    	    this.font = font;
    	}
    	
        glyphs = new HashMap<>();
        texture = createFontTexture(font, antiAlias);
    }

    /**
     * Creates a font texture from specified AWT font.
     *
     * @param font      The AWT font
     * @param antiAlias Wheter the font should be antialiased or not
     *
     * @return Font texture
     */
    private CharTexture createFontTexture(java.awt.Font font, boolean antiAlias) {
        /* Loop through the characters to get charWidth and charHeight */
        int imageWidth = 0;
        int imageHeight = 0;

        /* Start at char #32, because ASCII 0 to 31 are just control codes */
        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                /* ASCII 127 is the DEL control code, so we can skip it */
                continue;
            }
            char c = (char) i;
            BufferedImage ch = createCharImage(font, c, antiAlias);
            if (ch == null) {
                /* If char image is null that font does not contain the char */
                continue;
            }

            imageWidth += ch.getWidth();
            imageHeight = Math.max(imageHeight, ch.getHeight());
        }

        fontHeight = imageHeight;

        /* Image for the texture */
        BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        int x = 0;

        /* Create image for the standard chars, again we omit ASCII 0 to 31
         * because they are just control codes */
        for (int i = 32; i < 256; i++) {
            if (i == 127) {
                /* ASCII 127 is the DEL control code, so we can skip it */
                continue;
            }
            char c = (char) i;
            BufferedImage charImage = createCharImage(font, c, antiAlias);
            if (charImage == null) {
                /* If char image is null that font does not contain the char */
                continue;
            }

            int charWidth = charImage.getWidth();
            int charHeight = charImage.getHeight();

            /* Create glyph and draw char on image */
            Glyph ch = new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight);
            g.drawImage(charImage, x, 0, null);
            x += ch.width;
            glyphs.put(c, ch);
        }

//        /* Flip image Horizontal to get the origin to bottom left */
//        AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
//        transform.translate(0, -image.getHeight());
//        AffineTransformOp operation = new AffineTransformOp(transform,
//                                                            AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
//        image = operation.filter(image, null);
//
//        /* Get charWidth and charHeight of image */
//        int width = image.getWidth();
//        int height = image.getHeight();
//
//        /* Get pixel data of image */
//        int[] pixels = new int[width * height];
//        image.getRGB(0, 0, width, height, pixels, 0, width);
//
//        /* Put pixel data into a ByteBuffer */
//        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                /* Pixel as RGBA: 0xAARRGGBB */
//                int pixel = pixels[i * width + j];
//                /* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
//                buffer.put((byte) ((pixel >> 16) & 0xFF));
//                /* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
//                buffer.put((byte) ((pixel >> 8) & 0xFF));
//                /* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
//                buffer.put((byte) (pixel & 0xFF));
//                /* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
//                buffer.put((byte) ((pixel >> 24) & 0xFF));
//            }
//        }
//        /* Do not forget to flip the buffer! */
//        buffer.flip();

        CharTexture atlas = new CharTexture(TEXTURE_NAME, true, image);
        return atlas;
        
    }

    /**
     * Creates a char image from specified AWT font and char.
     *
     * @param font      The AWT font
     * @param c         The char
     * @param antiAlias Wheter the char should be antialiased or not
     *
     * @return Char image
     */
    private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
        /* Creating temporary image to extract character size */
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics();
        g.dispose();

        /* Get char charWidth and charHeight */
        int charWidth = metrics.charWidth(c);
        int charHeight = metrics.getHeight();

        /* Check if charWidth is 0 */
        if (charWidth == 0) {
            return null;
        }

        /* Create image for holding the char */
        image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
        g = image.createGraphics();
        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(font);
        g.setPaint(java.awt.Color.WHITE);
        g.drawString(String.valueOf(c), 0, metrics.getAscent());
        g.dispose();
        return image;
    }
    
    /**
     * Gets the width of the specified text.
     *
     * @param text The text
     *
     * @return Width of text
     */
    public int getWidth(CharSequence text) {
        int width = 0;
        int lineWidth = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                /* Line end, set width to maximum from line width and stored
                 * width */
                width = Math.max(width, lineWidth);
                lineWidth = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = glyphs.get(c);
            lineWidth += g.width;
        }
        width = Math.max(width, lineWidth);
        return width;
    }

    /**
     * Gets the height of the specified text.
     *
     * @param text The text
     *
     * @return Height of text
     */
    public int getHeight(CharSequence text) {
        int height = 0;
        int lineHeight = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\n') {
                /* Line end, add line height to stored height */
                height += lineHeight;
                lineHeight = 0;
                continue;
            }
            if (c == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = glyphs.get(c);
            lineHeight = Math.max(lineHeight, g.height);
        }
        height += lineHeight;
        return height;
    }

    /**
     * Draw text at the specified position and color.
     *
     * @param renderer The renderer to use
     * @param text     Text to draw
     * @param x        X coordinate of the text position
     * @param y        Y coordinate of the text position
     * @param c        Color to use
     */
    public CharRenderer[] drawText(CharSequence text, WindowCoordinates coordinates, Color colour) {
        CharRenderer[] characters = new CharRenderer[text.length()];
        
        int textHeight = getHeight(text);

        int drawX = (int) coordinates.getWindowCoordinatesX();
        int drawY = (int) coordinates.getWindowCoordinatesY();
        if (textHeight > fontHeight) {
            drawY += textHeight - fontHeight;
        }

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '\n') {
                /* Line feed, set x and y to draw at the next line */
                drawY += fontHeight;
                drawX = (int) coordinates.getWindowCoordinatesX();
                continue;
            }
            if (ch == '\r') {
                /* Carriage return, just skip it */
                continue;
            }
            Glyph g = glyphs.get(ch);
            characters[i] = new CharRenderer(new WindowCoordinates(drawX, drawY), g.width, g.height, texture, ch);
            characters[i].setColour(colour);
            
            WindowCoordinates a = new WindowCoordinates(drawX, drawY);
            WindowCoordinates b = new WindowCoordinates(drawX + g.width, drawY);
            WindowCoordinates c = new WindowCoordinates(drawX + g.width, drawY + g.height);
            WindowCoordinates d = new WindowCoordinates(drawX, drawY + g.height);
            
            final Supplier<Character> character = characters[i]::getCharacter;
            RenderingUtils.createQuad(a, b, c, d, colour, texture, Optional.of(() -> texture.getCurrentFrame(character)), characters[i]::getModelMatrix);
            
            drawX += g.width;
        }
        
        return characters;
    }

    /**
     * Draw text at the specified position.
     *
     * @param renderer The renderer to use
     * @param text     Text to draw
     * @param x        X coordinate of the text position
     * @param y        Y coordinate of the text position
     */
    public void drawText(CharSequence text, WindowCoordinates coordinates) {
        drawText(text, coordinates, Color.WHITE);
    }
    
    public String getFontName() {
    	return font.getFontName();
    }
    
    public int getFontStyle() {
    	return font.getStyle();
    }
    
    public int getFontSize() {
    	return font.getSize();
    }
    
    private class CharTexture extends Texture {
        
        public CharTexture(String textureName, boolean slick) {
            super(textureName, slick);
        }
        
        public CharTexture(String textureName, boolean slick, Image image) {
            super(textureName, slick, image);
        }
        
        public int getCurrentFrame(Supplier<Character> character) {
            return (int) ((float)glyphs.get(character.get()).x / this.getAsBufferedImage().getWidth() * glyphs.size());
        }
        
        @Override
        public int count() {
            return glyphs.size();
        }
        
    }
    
    public class CharRenderer extends RectangularObjectRenderer {
        
        private char character;
        
        public CharRenderer(WindowCoordinates coordinates, double width, double height, char character) {
            super(coordinates, width, height);
            
            this.character = character;
        }
        
        public CharRenderer(WindowCoordinates coordinates, double width, double height, Texture texture, char character) {
            super(coordinates, width, height, texture);
            
            this.character = character;
        }
        
        public char getCharacter() {
            return character;
        }
        
        public void setCharacter(char character) {
            this.character = character;
        }
    }
}