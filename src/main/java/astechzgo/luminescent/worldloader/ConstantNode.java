package astechzgo.luminescent.worldloader;

class ConstantNode extends EquationNode {
    private final double constant;

    ConstantNode(double constant) {
        this.constant = constant;
    }

    @Override
    public double resolve() {
        return constant;
    }
}
