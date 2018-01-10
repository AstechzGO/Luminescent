package astechzgo.luminescent.neuralnetwork;

import astechzgo.luminescent.entity.AIPlayer;
import astechzgo.luminescent.entity.Entity;
import astechzgo.luminescent.rendering.CircularObjectRenderer;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class NeuralNet {
    public AIPlayer player;
    public Entity targettedEntity;
    ArrayList<Node> inputLayer = new ArrayList<Node>();
    ArrayList<Node> hiddenLayer = new ArrayList<Node>();
    ArrayList<Node> outputLayer = new ArrayList<Node>();
    public NeuralNet(int numInputs, int numHiddens, int numOutputs) {
        for(int i = 0; i < numInputs; i++) {
            inputLayer.add(new Node());
        }
        for(int i = 0; i < numHiddens; i++) {
            hiddenLayer.add(new Node());
        }
        for(int i = 0; i < numOutputs; i++) {
            outputLayer.add(new Node());
        }
    }
    public void setInputs(double[] inputs) {
        for(int i = 0; i < inputs.length; i++) {
            inputLayer.get(i).calculateOutput(inputs[i]);
        }
    }
    public void setNodes(Node[] currentNodes, Node[] previousNodes) {
        for(int i = 0; i < currentNodes.length; i++) {
                currentNodes[i].calculateOutput(previousNodes);
        }
    }
    public void useOutputs(Node[] outputNodes) {
        if(outputNodes[0].getOutput() > 0.5) {
            player.turnRight();
        }
        if(outputNodes[1].getOutput() > 0.5) {
            player.shoot();
        }
        if(outputNodes[2].getOutput() > 0.5) {
            player.moveForward();
        }
    }
    public void updateNetwork() {
        double[] inputs = {rotationInput(), distanceInput()};
        setInputs(inputs);
        setNodes(hiddenLayer.toArray(new Node[0]), inputLayer.toArray(new Node[0]));
        setNodes(outputLayer.toArray(new Node[0]), hiddenLayer.toArray(new Node[0]));
        useOutputs(outputLayer.toArray(new Node[0]));
    }

    double lastEnd;

    public void retrainNetworks(boolean win) {
        double timeTaken = GLFW.glfwGetTime() - lastEnd;
        lastEnd = GLFW.glfwGetTime();

        for(Node n : inputLayer) {
            n.adjustToError(timeTaken, win);
        }
        for(Node n : hiddenLayer) {
            n.adjustToError(timeTaken, win);
        }
        for(Node n : outputLayer) {
            n.adjustToError(timeTaken, win);
        }
    }

    public double rotationInput() {
        double slope = (targettedEntity.getCoordinates().getGameCoordinatesZ() - player.getCoordinates().getGameCoordinatesZ()) / (targettedEntity.getCoordinates().getGameCoordinatesX() - player.getCoordinates().getGameCoordinatesX());
        double angle = 0;

        if(slope == Double.POSITIVE_INFINITY) {
            angle = 180;
        }
        else if(slope == Double.NEGATIVE_INFINITY) {
            angle = 0;
        }
        if(player.getCoordinates().getGameCoordinatesX() == targettedEntity.getCoordinates().getGameCoordinatesX() && player.getCoordinates().getGameCoordinatesZ() == targettedEntity.getCoordinates().getGameCoordinatesZ()) {
            angle = 0;
        }
        else if(player.getCoordinates().getGameCoordinatesX() > targettedEntity.getCoordinates().getGameCoordinatesX())
            if(90 + Math.toDegrees(Math.atan(slope)) > 360)
               angle = Math.toDegrees(Math.atan(slope)) + 90 - 360;
            else
                angle = Math.toDegrees(Math.atan(slope)) + 90;
        else {
            if (Math.toDegrees(Math.atan(slope)) - 90 > 360)
                angle = Math.toDegrees(Math.atan(slope)) - 90 - 360;
            else
                angle = Math.toDegrees(Math.atan(slope)) - 90;
        }

        double playerRotation = ((CircularObjectRenderer)player.getRenderer()).getRotation();

        double diff = (angle + 720 - playerRotation) % 360;

        if(diff > 180) {
            diff -= 360;
        }

        diff *= -1;

        return diff;


    }

    public double distanceInput() {
        double x = targettedEntity.getCoordinates().getGameCoordinatesX() - player.getCoordinates().getGameCoordinatesX();
        double z = targettedEntity.getCoordinates().getGameCoordinatesZ() -  player.getCoordinates().getGameCoordinatesZ();

        return Math.sqrt(x * x + z * z);
    }
}
