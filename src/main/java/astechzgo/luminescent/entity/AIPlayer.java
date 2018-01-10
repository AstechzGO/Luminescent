package astechzgo.luminescent.entity;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.CircularObjectRenderer;
import org.lwjgl.glfw.GLFW;

public class AIPlayer extends Player {

    private boolean turning, shooting, movingForward;
    private double lastDelta = 0;

    public AIPlayer(GameCoordinates coordinates) {
        super(new CircularObjectRenderer(new WindowCoordinates(coordinates), 40, 1));
        lastDelta = GLFW.glfwGetTime() * 1000;
    }

    @Override
    public MovementInfo getMove() {
        double delta = ((GLFW.glfwGetTime() * 1000) - lastDelta);
        lastDelta = GLFW.glfwGetTime() * 1000;

        MovementInfo move = new MovementInfo(movingForward, false, false, false, false, turning ? getRotation() + 0.5 * delta : getRotation(), false);
        turning = false;
        shooting = false;
        movingForward = false;
        return move;
    }

    private void turnRight() {
        turning = true;
    }

    private void shoot() {
        shooting = true;
    }

    private void moveForward() {
        movingForward = true;
    }
}
