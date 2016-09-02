package astechzgo.luminescent.rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.seisw.util.geom.Poly;

import astechzgo.luminescent.coordinates.GameCoordinates;

public class Polygon {

	private final List<GameCoordinates> coordinates;
	private final List<Edge> edges;
	private final List<Polygon> holes;
	private final List<Edge> edgesWithHoles;
	
	private static GameCoordinates[] fromPoly(Poly poly) {
		GameCoordinates[] coordinates = new GameCoordinates[poly.getNumPoints()];
		for(int i = 0; i < poly.getNumPoints(); i++) {
			coordinates[i] = new GameCoordinates(poly.getX(i), poly.getY(i));
		}
		
		return coordinates;
	}
	
	
	public Polygon(Poly poly) {
		this.coordinates = Arrays.asList(fromPoly(poly));
		
		List<Edge> edges = new ArrayList<Edge>(this.coordinates.size() / 2);
		
		for(int i = 0; i < this.coordinates.size(); i++) {
			// Next point if possible, if not, first point
			int nextIdx = i + 1 < this.coordinates.size() ? i + 1 : 0;
			
			edges.add(new Edge(this.coordinates.get(i), this.coordinates.get(nextIdx)));
		}
		
		List<Polygon> holes = new ArrayList<Polygon>();
		
		for(int i = 0; i < poly.getNumInnerPoly(); i++) {
			Poly innerPoly = poly.getInnerPoly(i);
			
			if(innerPoly.isHole()) {
				holes.add(new Polygon(innerPoly));
			}
		}
		
		List<Edge> edgesWithHoles = new ArrayList<Edge>();
		edgesWithHoles.addAll(edges);
		for(Polygon hole : holes) {
			edgesWithHoles.addAll(hole.edges);
		}
		
		this.holes = holes;
		
		this.edges = edges;
		this.edgesWithHoles = edgesWithHoles;
		
	}
	
	public List<GameCoordinates> getCoordinates() {
		return coordinates;
	}
	
	public List<Edge> getEdges() {
		return getEdges(false);
	}
	
	public List<Edge> getEdges(boolean withHoles) {
		if(withHoles) {
			return edgesWithHoles;
		}
		else {
			return edges;
		}
	}
	
	public List<Polygon> getHoles() {
		return holes;
	}
	
	public class Edge {
		
		public final GameCoordinates first, second;
		
		public Edge(GameCoordinates first, GameCoordinates second) {
			this.first = first;
			this.second = second;
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Edge && first.equals(((Edge)obj).first) && second.equals(((Edge)obj).second);
		}
		
	}
}
