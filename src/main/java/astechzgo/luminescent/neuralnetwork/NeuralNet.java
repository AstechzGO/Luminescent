package astechzgo.luminescent.neuralnetwork;

import java.util.ArrayList;

public class NeuralNet {
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
      //  if(outputNodes[0] < 0.5)
    }
    public void updateNetwork() {
        double[] inputs = {rotationInput(), distanceInput()};
        setInputs(inputs);
    }
    public double rotationInput() {
        return 0;
    }
    public double distanceInput() {
        return 0;
    }
}
