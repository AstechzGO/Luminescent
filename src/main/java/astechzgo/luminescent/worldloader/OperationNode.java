package astechzgo.luminescent.worldloader;

import java.util.Objects;

class OperationNode extends EquationNode {

    private final OperationType type;
    private final EquationNode a, b;

    OperationNode(OperationType type, EquationNode a, EquationNode b) {
        this.type = Objects.requireNonNull(type);
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
    }

    @Override
    public double resolve() {
        return  type.apply(a.resolve(), b.resolve());
    }
}
