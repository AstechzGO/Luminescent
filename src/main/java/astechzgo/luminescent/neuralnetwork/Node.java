package astechzgo.luminescent.neuralnetwork;

public class Node {
    public double outputWeight = 1;
    public double minimumFiringThreshold = 0;
    double output = 0;
    public Node() {
        generateRandomWeight();
    }
    public void generateRandomWeight() {
        outputWeight = ((Math.random() * 2.0) - 1);
    }
    public void calculateOutput(Node[] connectedInputNodes) {
       output = 0;
        for(int i = 0; i < connectedInputNodes.length; i++) {
            output += connectedInputNodes[i].getOutput() * connectedInputNodes[i].outputWeight;
        }
        output = sigmoid(output);
    }
    public void calculateOutput(double inputLayer) {
        output = inputLayer;
        output = sigmoid(output);
    }
    public double sigmoid(double x) {
        return (1.0 / (1 + Math.exp(-x)));
    }
    public double sigmoid_derivative(double x) {
        return x * (1 - x);
    }
    public double getOutput() {
        return output;
    }
    public double previousAdjustment = 0;
    public double previousTimeTaken = 0;
    public double learnRate = 0.3;
    public void adjustToError(double timeTaken, boolean win) {
        double previousOutput = outputWeight;
        if(win) {
            if(previousTimeTaken < timeTaken) {
                if(previousAdjustment > 0) {
                    outputWeight += timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
                else {
                    outputWeight -= timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
            }
            else {
                if(previousAdjustment > 0) {
                    outputWeight -= timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
                else {
                    outputWeight += timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
            }
        }
        else {
            if(previousTimeTaken > timeTaken) {
                if(previousAdjustment > 0) {
                    outputWeight += timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
                else {
                    outputWeight -= timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
            }
            else {
                if(previousAdjustment > 0) {
                    outputWeight -= timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
                else {
                    outputWeight += timeTaken * learnRate * sigmoid_derivative(getOutput());
                }
            }
        }
        previousTimeTaken = timeTaken;
        previousAdjustment = outputWeight - previousOutput;
    }
}
