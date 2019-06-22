package astechzgo.luminescent.worldloader;

import java.util.function.BiFunction;

enum OperationType implements BiFunction<Double, Double, Double> {
    MULTIPLY((a, b) -> a * b, Integer.MAX_VALUE - 1),
    DIVIDE((a, b) -> a / b, Integer.MAX_VALUE - 1),
    ADD(Double::sum, Integer.MAX_VALUE),
    SUBTRACT((a, b) -> a - b, Integer.MAX_VALUE);

    private final BiFunction<Double, Double, Double> operation;
    private final int priority;

    OperationType(BiFunction<Double, Double, Double> operation, int priority) {
        this.operation = operation;
        this.priority = priority;
    }

    @Override
    public Double apply(Double a, Double b) {
        return operation.apply(a, b);
    }

    public int getPriority() {
        return priority;
    }
}
