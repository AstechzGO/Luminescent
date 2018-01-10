package astechzgo.luminescent.neuralnetwork;

import astechzgo.luminescent.entity.AIPlayer;
import astechzgo.luminescent.rendering.CircularObjectRenderer;

import java.util.ArrayList;

public class NeuralNet {
    public AIPlayer player;
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
    }
    public void retrainNetworks(boolean win, double timeTaken) {

    }

    public double rotationInput() {
        return 0;
    }
    public double distanceInput() {
        return 0;
    }
}
