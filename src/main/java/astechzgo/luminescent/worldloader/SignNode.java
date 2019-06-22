package astechzgo.luminescent.worldloader;

class SignNode extends EquationNode {

    private final boolean negative;
    private final EquationNode node;

    SignNode(boolean negative, EquationNode node) {
        this.negative = negative;
        this.node = node;
    }

    @Override
    public double resolve() {
        if(negative) {
            return -node.resolve();
        }
        else {
            return +node.resolve();
        }
    }
}
