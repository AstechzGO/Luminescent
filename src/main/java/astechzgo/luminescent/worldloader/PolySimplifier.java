package astechzgo.luminescent.worldloader;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.gameobject.Room;

import java.util.*;
import java.util.stream.Collectors;

class PolySimplifier {

    public static List<Edge> simplify(List<Room> rooms) {
        Rectangle[] rectangles = new Rectangle[rooms.size()];
        for(int i = 0; i < rectangles.length; i++) {
            Room room = rooms.get(i);
            GameCoordinates coords = new GameCoordinates(room.getCoordinates());

            rectangles[i] = new Rectangle(coords.getGameCoordinatesX(), coords.getGameCoordinatesZ(), room.getWidth(), room.getHeight());
        }

        return simplify(rectangles);
    }

    private static List<Edge> mergeIntervals(List<Edge> edges, boolean horizontal) {
        Deque<Edge> stack = new ArrayDeque<>();

        Comparator<Edge> comp;
        if(horizontal) {
            comp = Comparator.comparingDouble(p -> p.x);
        }
        else {
            comp = Comparator.comparingDouble(p -> p.y);
        }

        edges.sort(comp);

        if (edges.size() < 1) {
            return edges;
        }
        stack.push(edges.get(0));

        for (int j = 1; j < edges.size(); j++) {
            Edge i = edges.get(j);
            Edge top  = Objects.requireNonNull(stack.peek(), "Top of Edge stack can't be null");

            double iStart = horizontal ? i.x : i.y;
            double topStart = horizontal ? top.x : top.y;

            if ((topStart + top.length < iStart)) {
                stack.push(i);
            }
            else if (topStart + top.length < iStart + i.length) {
                top.length = iStart + i.length - topStart;
            }
        }
        return new ArrayList<>(stack);

    }

    private static List<Edge> simplify(Rectangle...rectangles) {
        List<Edge> edges = new ArrayList<>(rectangles.length * 4);

        Arrays.stream(rectangles)
                .flatMap((rectangle) -> Arrays.stream(rectangle.edges()))
                .distinct()
                .collect(Collectors.toCollection(() -> edges));

        var partition = edges.stream().collect(Collectors.partitioningBy((edge) -> edge.horizontal));

        var x = partition.get(true).stream().collect(Collectors.groupingBy((edge) -> edge.y));
        var y = partition.get(false).stream().collect(Collectors.groupingBy((edge) -> edge.x));

        for (var combined : x.values()) {
            if (combined.size() == 0) {
                continue;
            }

            List<Edge> newEdges = mergeIntervals(combined, true);
            edges.removeIf(combined::contains);
            edges.addAll(newEdges);
        }

        for (var combined : y.values()) {
            if (combined.size() == 0) {
                continue;
            }

            List<Edge> newEdges = mergeIntervals(combined, false);
            edges.removeIf(combined::contains);
            edges.addAll(newEdges);
        }

        Map<Edge, SortedSet<Double>> hIntersect = new HashMap<>();
        Map<Edge, SortedSet<Double>> vIntersect = new HashMap<>();

        partition = edges.stream().collect(Collectors.partitioningBy((edge) -> edge.horizontal));

        var xEdges = partition.get(true);
        var yEdges = partition.get(false);

        for(Edge h : xEdges) {
            hIntersect.put(h, new TreeSet<>());

            for(Edge v : yEdges) {
                if(!vIntersect.containsKey(v)) {
                    vIntersect.put(v, new TreeSet<>());
                }

                if(h.x <= v.x && v.x <= h.x + h.length && v.y <= h.y && h.y <= v.y + v.length) {
                    hIntersect.get(h).add(v.x);
                    vIntersect.get(v).add(h.y);
                }
            }
        }

        for(Map.Entry<Edge, SortedSet<Double>> entry : hIntersect.entrySet()) {
            Edge e = entry.getKey();

            for(double intersect : entry.getValue()) {
                if(intersect != e.x && intersect != e.x + e.length) {
                    double end = e.x + e.length;
                    e.length = intersect - e.x;
                    e = new Edge(true, intersect, e.y, end - intersect);
                    edges.add(e);
                }
            }
        }

        for(Map.Entry<Edge, SortedSet<Double>> entry : vIntersect.entrySet()) {
            Edge e = entry.getKey();

            for(double intersect : entry.getValue()) {
                if(intersect != e.y && intersect != e.y + e.length) {
                    double end = e.y + e.length;
                    e.length = intersect - e.y;
                    e = new Edge(false, e.x, intersect, end - intersect);
                    edges.add(e);
                }
            }
        }

        partition = edges.stream().collect(Collectors.partitioningBy((edge) -> edge.horizontal));

        xEdges = partition.get(true);
        yEdges = partition.get(false);

        for(Edge edge : xEdges) {
            boolean hasBelow = false, hasAbove = false;
            for(Rectangle rectangle : rectangles) {
                if(edge.x >= rectangle.x && edge.x + edge.length <= rectangle.x + rectangle.width) {
                    if(rectangle.y < edge.y && edge.y < rectangle.y + rectangle.height) {
                        hasAbove = true;
                        hasBelow = true;
                    }
                    else if(edge.y == rectangle.y) {
                        hasBelow = true;
                    }
                    else if(edge.y == rectangle.y + rectangle.height) {
                        hasAbove = true;
                    }

                    if(hasBelow && hasAbove) {
                        break;
                    }
                }
            }

            if(hasAbove == hasBelow) {
                edges.remove(edge);
            }
        }

        for(Edge edge : yEdges) {
            boolean hasLeft = false, hasRight = false;
            for(Rectangle rectangle : rectangles) {
                if(edge.y >= rectangle.y && edge.y + edge.length <= rectangle.y + rectangle.height) {
                    if(rectangle.x < edge.x && edge.x < rectangle.x + rectangle.width) {
                        hasLeft = true;
                        hasRight = true;
                    }
                    else if(edge.x == rectangle.x) {
                        hasRight = true;
                    }
                    else if(edge.x == rectangle.x + rectangle.width) {
                        hasLeft = true;
                    }

                    if(hasLeft && hasRight) {
                        break;
                    }
                }
            }

            if(hasLeft == hasRight) {
                edges.remove(edge);
            }
        }

        return edges;
    }

    public static class Edge {
        private boolean horizontal;
        private double x, y;
        private double length;

        private Edge(boolean horizontal, double x, double y, double length) {
            this.horizontal = horizontal;
            this.x = x;
            this.y = y;
            this.length = length;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Edge && horizontal == ((Edge) other).horizontal && x == ((Edge) other).x && y == ((Edge) other).y && length == ((Edge) other).length;
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public boolean getHorizontal() {
            return horizontal;
        }

        public double getLength() {
            return length;
        }
    }

    private static class Rectangle {
        private double x, y;
        private double width, height;

        private Rectangle(double x, double y, double width, double height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private Edge[] edges() {
            return new Edge[]{
                    new Edge(true, x, y, width),
                    new Edge(false, x + width, y, height),
                    new Edge(true, x, y + height, width),
                    new Edge(false, x, y, height)
            };
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Rectangle && x == ((Rectangle) other).x && y == ((Rectangle) other).y && width == ((Rectangle) other).width && height == ((Rectangle) other).height;
        }
    }
}
