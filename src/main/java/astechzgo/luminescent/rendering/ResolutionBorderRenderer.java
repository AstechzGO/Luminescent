package astechzgo.luminescent.rendering;

import java.awt.Color;
import java.awt.Rectangle;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.utils.DisplayUtils;

public class ResolutionBorderRenderer extends RectangularObjectRenderer {
    
    public static final Rectangle LEFT_RECTANGLE = new Rectangle(-1, 0, 1, (int) Camera.CAMERA_HEIGHT);
    public static final Rectangle RIGHT_RECTANGLE = new Rectangle((int) Camera.CAMERA_WIDTH, 0, 1, (int) Camera.CAMERA_HEIGHT);
    public static final Rectangle TOP_RECTANGLE = new Rectangle(0, -1, (int) Camera.CAMERA_WIDTH, 1);
    public static final Rectangle BOTTOM_RECTANGLE = new Rectangle(0, (int) Camera.CAMERA_HEIGHT, (int) Camera.CAMERA_WIDTH, 1);

    private final boolean leftRight;
    
    public ResolutionBorderRenderer(Rectangle rectangle) {
        super(new WindowCoordinates(rectangle.x, rectangle.y), rectangle.width, rectangle.height);
        this.setColour(Color.BLACK);
        
        if(rectangle == LEFT_RECTANGLE || rectangle == RIGHT_RECTANGLE) {
            leftRight = true;
        }
        else if(rectangle == TOP_RECTANGLE || rectangle == BOTTOM_RECTANGLE) {
            leftRight = false;
        }
        else {
            throw new IllegalArgumentException("Rectangle must be left, right, top, or bottom");
        }
    }
    
    @Override
    public void resize() {
        super.a = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY());
        super.b = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY());
        super.c = new WindowCoordinates(coordinates.getWindowCoordinatesX() + width, coordinates.getWindowCoordinatesY() + height);
        super.d = new WindowCoordinates(coordinates.getWindowCoordinatesX(), coordinates.getWindowCoordinatesY() + height);
        
        oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
        oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;
        
        int x = (int) new ScaledWindowCoordinates(coordinates).getScaledWindowCoordinatesX() + DisplayUtils.widthOffset;
        int y = (int) new ScaledWindowCoordinates(coordinates).getScaledWindowCoordinatesY() + DisplayUtils.heightOffset;
        
        if(this.getCoordinates().getWindowCoordinatesX() == -1) {
            x = 0;
        }
        else if(this.getCoordinates().getWindowCoordinatesX() == Camera.CAMERA_WIDTH + 1) {
            x = DisplayUtils.getDisplayWidth();
        }
        
        if(this.getCoordinates().getWindowCoordinatesY() == -1) {
            y = 0;
        }
        else if(this.getCoordinates().getWindowCoordinatesY() == Camera.CAMERA_HEIGHT + 1) {
            y = DisplayUtils.getDisplayHeight();
        }
        
        ScaledWindowCoordinates loc = new ScaledWindowCoordinates(x, y);
        Vector3f location = new Vector3f((float) loc.getScaledWindowCoordinatesX(), (float) loc.getScaledWindowCoordinatesY(), 0.0f);
        
        
        Matrix4f model = new Matrix4f().translation(location);
        
        float scaleFactor = (float) (1.0 / Camera.CAMERA_WIDTH * DisplayUtils.getDisplayWidth());
        
        if(leftRight) {
            if(DisplayUtils.widthOffset == 0) {
                model.scale(0.0f);
            }
            else {
                model.scale((float) (DisplayUtils.widthOffset /  width), scaleFactor, 1.0f);
            }
        }
        else {
            if(DisplayUtils.heightOffset == 0) {
                model.scale(0.0f);
            }
            else {
                model.scale(scaleFactor, (float) (DisplayUtils.heightOffset /  height), 1.0f);
            }
        }
        
        this.model = model;
    }
}
