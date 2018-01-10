package astechzgo.luminescent.entity;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.keypress.Key;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.CircularObjectRenderer;
import astechzgo.luminescent.textures.Texture;
import astechzgo.luminescent.utils.Constants;
import astechzgo.luminescent.utils.ControllerUtils;
import astechzgo.luminescent.utils.DisplayUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;

import java.nio.DoubleBuffer;

public class HumanPlayer extends Player
{
    private double lastControllerDelta = 0;
    private ScaledWindowCoordinates lastMouseCoords;

    public HumanPlayer() {
        super(new PlayerRenderer(new WindowCoordinates(new GameCoordinates(Camera.CAMERA_WIDTH / 2, Camera.CAMERA_HEIGHT / 2)), 40, 1));

        lastControllerDelta = GLFW.glfwGetTime() * 1000;
    }

    public double setRotation() {
        double delta = ((GLFW.glfwGetTime() * 1000) - lastControllerDelta);
        lastControllerDelta = GLFW.glfwGetTime() * 1000;

        if(ControllerUtils.isButtonPressed(Constants.CONTROLLER_MOVEMENT_ROTATION_CLOCKWISE)) {
            setRotation(getRotation() + 0.5 * delta);

            return getRotation();
        }
        if(ControllerUtils.isButtonPressed(Constants.CONTROLLER_MOVEMENT_ROTATION_COUNTERCLOCKWISE)) {
            setRotation(getRotation() - 0.5 * delta);

            return getRotation();
        }

        ScaledWindowCoordinates mouseCoords = null;

        try(MemoryStack stack = MemoryStack.stackPush()) {
            DoubleBuffer mxpos = stack.mallocDouble(1);
            DoubleBuffer mypos = stack.mallocDouble(1);

            GLFW.glfwGetCursorPos(DisplayUtils.getHandle(), mxpos, mypos);

            mouseCoords = new ScaledWindowCoordinates(mxpos.get(0) - DisplayUtils.widthOffset, mypos.get(0) - DisplayUtils.heightOffset);

            if(mouseCoords.equals(lastMouseCoords))
                return getRotation();

            lastMouseCoords = mouseCoords;

            mxpos.clear();
            mypos.clear();
        }
        double m = (coordinates.getAbsoluteY() - mouseCoords.getAbsoluteY()) / (coordinates.getAbsoluteX() - mouseCoords.getAbsoluteX());

        if(m == Double.POSITIVE_INFINITY) {
            setRotation(180);
            return getRotation();
        }

        if(m == Double.NEGATIVE_INFINITY) {
            setRotation(0);
            return getRotation();
        }
        if(mouseCoords.getAbsoluteX() == coordinates.getAbsoluteX() && mouseCoords.getAbsoluteY() == coordinates.getAbsoluteY()) {
            return getRotation();
        }

        if(mouseCoords.getAbsoluteX() < coordinates.getAbsoluteX())
            if(90 + Math.toDegrees(Math.atan(m)) > 360)
                setRotation(Math.toDegrees(Math.atan(m)) + 90 - 360);
            else
                setRotation(Math.toDegrees(Math.atan(m)) + 90);
        else {
            if(Math.toDegrees(Math.atan(m)) - 90 > 360)
                setRotation(Math.toDegrees(Math.atan(m)) - 90 - 360);
            else
                setRotation(Math.toDegrees(Math.atan(m)) - 90);
        }

        return getRotation();
    }

    @Override
    public MovementInfo getMove() {
        return new MovementInfo(Key.KEYS_MOVEMENT_UP.isKeyDown(), Key.KEYS_MOVEMENT_DOWN.isKeyDown(), Key.KEYS_MOVEMENT_RIGHT.isKeyDown(), Key.KEYS_MOVEMENT_LEFT.isKeyDown(), Key.KEYS_MOVEMENT_FASTER.isKeyDown(), setRotation(), Key.KEYS_ACTION_SHOOT.isKeyDown());
    }

    private static class PlayerRenderer extends CircularObjectRenderer {

        private PlayerRenderer(WindowCoordinates coordinates, double radius) {
            super(coordinates, radius);
        }

        private PlayerRenderer(WindowCoordinates coordinates, double radius, int pointSeperation) {
            super(coordinates, radius, pointSeperation);
        }

        private PlayerRenderer(WindowCoordinates coordinates, double radius, int pointSeperation, Texture texture) {
            super(coordinates, radius, pointSeperation, texture);
        }

        private PlayerRenderer(WindowCoordinates coordinates, double radius, Texture texture) {
            super(coordinates, radius, texture);
        }

        @Override
        public void resize() {
            coordinates = new WindowCoordinates(Camera.CAMERA_WIDTH / 2, Camera.CAMERA_HEIGHT / 2);

            oldGameWidth = DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2;
            oldGameHeight = DisplayUtils.getDisplayHeight() - DisplayUtils.heightOffset * 2;

            ScaledWindowCoordinates loc = new ScaledWindowCoordinates(coordinates);
            Vector3f location = new Vector3f((float)loc.getScaledWindowCoordinatesX() + DisplayUtils.widthOffset, (float)loc.getScaledWindowCoordinatesY()  + DisplayUtils.heightOffset, 0.0f);

            Quaternionf rotate = new Quaternionf().rotateZ((float) Math.toRadians(rotation));

            this.model = new Matrix4f().translation(location).rotateAround(rotate, 0, 0, 0).scale((float) (1.0 / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2)));
        }

    }

}
