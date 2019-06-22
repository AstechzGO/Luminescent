package astechzgo.luminescent.worldloader;

import java.nio.charset.MalformedInputException;
import java.util.*;
import java.util.regex.Pattern;

class EquationInterpreter {
    private final Map<String, Double> substitutions;

    EquationInterpreter(Map<String, Double> substitutions) {
        this.substitutions = substitutions;
    }

    double resolve(String equation) throws MalformedInputException {
        Objects.requireNonNull(equation);

        for(Map.Entry<String, Double> substitution : substitutions.entrySet()) {
            equation = equation.replaceAll(substitution.getKey(), String.valueOf(substitution.getValue()));
        }
        equation = equation.replaceAll("\\s", "");

        StringBuilder sb = new StringBuilder(equation);

        return makeNode(sb, 0).resolve();
    }

    private EquationNode makeNode(StringBuilder data, int start) throws MalformedInputException {
        List<EquationNode> surfaceNodes = new ArrayList<>();
        List<OperationType> operationTypes = new ArrayList<>();

        boolean lastWasNode = false;

        while (data.length() > 0) {
            String result = readToken(data, start);
            switch (result) {
                case "(":
                    surfaceNodes.add(getNextTerm(data, start));
                    lastWasNode = true;
                    break;
                case ")":
                    return amiguousNodes(surfaceNodes, operationTypes);
                case "*":
                    operationTypes.add(OperationType.MULTIPLY);
                    data.delete(start, start+1);
                    lastWasNode = false;
                    break;
                case "/":
                    operationTypes.add(OperationType.DIVIDE);
                    data.delete(start, start+1);
                    lastWasNode = false;
                    break;
                case "+":
                    if(lastWasNode) {
                        operationTypes.add(OperationType.ADD);
                        data.delete(start, start+1);
                        lastWasNode = false;
                    }
                    else {
                        surfaceNodes.add(getNextTerm(data, start));
                    }
                    break;
                case "-":
                    if(lastWasNode) {
                        operationTypes.add(OperationType.SUBTRACT);
                        data.delete(start, start+1);
                        lastWasNode = false;
                    }
                    else {
                        surfaceNodes.add(getNextTerm(data, start));
                    }
                    break;
                default:
                    surfaceNodes.add(getNextTerm(data, start));
                    lastWasNode = true;
            }
        }

        return amiguousNodes(surfaceNodes, operationTypes);
    }

    private EquationNode amiguousNodes(List<EquationNode> surfaceNodes, List<OperationType> operations) {
        if(operations.size() == 0 && surfaceNodes.size() == 1) {
            return surfaceNodes.get(0);
        }

        int index = 0;
        OperationType operationType = null;

        for(int i = 0; i < operations.size(); i++) {
            if(operationType == null || operations.get(i).getPriority() < operationType.getPriority()) {
                index = i;
                operationType = operations.get(i);
            }
        }

        OperationNode node = new OperationNode(operationType, surfaceNodes.get(index), surfaceNodes.get(index + 1));
        surfaceNodes.set(index, node);
        surfaceNodes.remove(index + 1);
        operations.remove(index);
        return amiguousNodes(surfaceNodes, operations);
    }

    private EquationNode getNextTerm(StringBuilder data, int start) throws MalformedInputException {
        String result = readToken(data, start);
        switch (result) {
            case "(":
                EquationNode node = makeNode(data, start + 1);
                data.delete(start, start + 1);
                return node;
            case "+":
                node = getNextTerm(data, start + 1);
                data.delete(start, start+1);
                return new SignNode(false, node);
            case "-":
                node = getNextTerm(data, start + 1);
                data.delete(start, start+1);
                return new SignNode(true, node);
            default:
                node = new ConstantNode(readDouble(result));
                data.delete(start, start + result.length());
                return node;
        }
    }

    private double readDouble(String data) throws MalformedInputException {
        try {
            return Double.parseDouble(data);
        }
        catch (NumberFormatException e) {
            MalformedInputException ex = new MalformedInputException(data.length());
            ex.initCause(e);
            throw ex;
        }
    }

    private String readToken(StringBuilder equation, int start) throws MalformedInputException {
        switch(equation.charAt(start)) {
            case '+':
                return "+";
            case '-':
                return "-";
            case '*':
                return "*";
            case '/':
                return "/";
            case '(':
                return "(";
            case ')':
                return ")";
            default:
                return readNumber(equation, start);
        }
    }

    private String readNumber(StringBuilder equation, int start) throws MalformedInputException {
        final Pattern pattern = Pattern.compile("^\\d+(\\.\\d)?\\d*");
        return pattern.matcher(equation.substring(start)).results().findAny().orElseThrow(() -> new MalformedInputException(equation.length())).group();
    }
}
