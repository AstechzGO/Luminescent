package astechzgo.luminescent.worldloader;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.gson.Gson;
import com.seisw.util.geom.Poly;
import com.seisw.util.geom.PolyDefault;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.WindowCoordinates;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.rendering.Camera;
import astechzgo.luminescent.rendering.QuadrilateralObjectRenderer;
import astechzgo.luminescent.textures.TextureList;

public class JSONWorldLoader {
	
	private String x;
	private String z;
	
	public String width;
	public String height;
	
	public static List<QuadrilateralObjectRenderer> lines;
	
	public static List<Room> loadRooms() {
		Gson g = new Gson();
		String parse = "";
		InputStream in = new TextureList().getClass().getResourceAsStream("/resources/world/DefaultWorld.json");
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		String line = "";
		try {
			while((line = input.readLine()) != null) {
				parse = parse + line;
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONWorldLoader[] loaders = g.fromJson(parse, JSONWorldLoader[].class);
		List<Room> rooms = new ArrayList<Room>();
		
		for(JSONWorldLoader loader : loaders) {
			rooms.add(new Room(loader));
		}
		
		List<Polygon> polygons = new ArrayList<Polygon>();
		for(Room room : rooms) {
			int[] x = new int[] {
				(int) (room.getCoordinates().getWindowCoordinatesX()),
				(int) (room.getCoordinates().getWindowCoordinatesX()),
				(int) (room.getCoordinates().getWindowCoordinatesX() + room.getWidth()),
				(int) (room.getCoordinates().getWindowCoordinatesX() + room.getWidth()),
			};
			
			int[] y = new int[] {
				(int) (room.getCoordinates().getWindowCoordinatesY()),
				(int) (room.getCoordinates().getWindowCoordinatesY() + room.getHeight()),
				(int) (room.getCoordinates().getWindowCoordinatesY() + room.getHeight()),
				(int) (room.getCoordinates().getWindowCoordinatesY())
			};
			polygons.add(new Polygon(x, y, 4));
		}
 		
		List<List<Polygon>> clusters = getClusters(polygons);
		
		List<astechzgo.luminescent.rendering.Polygon> gamePolygons = new ArrayList<astechzgo.luminescent.rendering.Polygon>();
		
		for(List<Polygon> cluster : clusters) {
			gamePolygons.add(new astechzgo.luminescent.rendering.Polygon(mergePolygons(cluster)));
			
		}
		
		lines = new ArrayList<QuadrilateralObjectRenderer>();
		
		for(astechzgo.luminescent.rendering.Polygon gamePolygon : gamePolygons) {
			for(astechzgo.luminescent.rendering.Polygon.Edge edge : gamePolygon.getEdges(true)) {
				WindowCoordinates firstBottom = new WindowCoordinates(edge.first);
				WindowCoordinates firstTop = new WindowCoordinates(new GameCoordinates(edge.first.getGameCoordinatesX() + 5, edge.first.getGameCoordinatesZ() + 5));
				WindowCoordinates secondBottom = new WindowCoordinates(edge.second);
				WindowCoordinates secondTop = new WindowCoordinates(new GameCoordinates(edge.second.getGameCoordinatesX() + 5, edge.second.getGameCoordinatesZ() + 5));
				QuadrilateralObjectRenderer renderer = new QuadrilateralObjectRenderer(firstBottom, firstTop, secondTop, secondBottom);
				renderer.setColour(Color.MAGENTA);
				lines.add(renderer);
			}
		}
		
		return rooms;
	}
	
	/**
	 * Gets the clusters of intersecting rectangles in a list of rectangles
	 *
	 * @param polygons The rectangles to find clusters in
	 * @return the clusters of rectangles
	 */
	private static List<List<Polygon>> getClusters(List<Polygon> polygons) {		
		List<List<Polygon>> clusters = new ArrayList<List<Polygon>>();
		
		List<List<Polygon>> intersectors = new ArrayList<List<Polygon>>();
		
		for(Polygon polygon : polygons) {
			intersectors.add(getAllIntersectors(polygon, polygons));
		}
		
		for(List<Polygon> intersection : intersectors) {
			
			boolean foundCluster = false;
			
			for(List<Polygon> cluster : clusters) {
				for(Polygon polygon : intersection) {
					if(cluster.contains(polygon)) {
						mergeIntersection(cluster, intersection);
						
						foundCluster = true;
						break;
					}
				}
			}
			
			if(!foundCluster) {
				clusters.add(intersection);
			}
		}
		
		return clusters;
	}
	
	private static void mergeIntersection(List<Polygon> cluster, List<Polygon> intersection) {
		for(Polygon polygon : intersection) {
			if(!cluster.contains(polygon)) {
				cluster.add(polygon);
			}
		}
	}
	
	private static List<Polygon> getAllIntersectors(Polygon starter, List<Polygon> polygons) {
		List<Polygon> clonePolygons = new ArrayList<Polygon>(polygons);
		
		if(clonePolygons.contains(starter))
			clonePolygons.remove(starter);
		
		List<Polygon> intersectors = new ArrayList<Polygon>();
		intersectors.add(starter);
		
		for(Polygon polygon : clonePolygons) {
			if(intersects(starter, polygon)) {
				intersectors.add(polygon);
			}
				
		}
		
		return intersectors;
	}
	
	private static boolean intersects(Polygon polygon1, Polygon polygon2) {
		Area area = new Area(polygon1);
		area.intersect(new Area(polygon2));
		return !area.isEmpty();
	}
	
	private static Poly mergePolygons(List<Polygon> polygons) {
		List<Poly> polys = new ArrayList<Poly>();
		
		for(Polygon p : polygons) {
			Poly poly = new PolyDefault();
			for(int i = 0; i < p.npoints; i++) {
				poly.add(new Point2D.Double(p.xpoints[i], p.ypoints[i]));
			}
			
			polys.add(poly);
			
		}
		
		Poly union = polys.get(0);
		
		
		for(int i = 1; i < polys.size(); i++) {
			union = union.union(polys.get(i));
		}
		
//		for(int i = 0; i < union.getNumInnerPoly(); i++) {
//			System.out.println("union.innerPoly.isHole() = " + union.getInnerPoly(i).isHole());
//		}
		
//		Polygon out = new Polygon();
//		for(int i = 0; i < union.getNumPoints(); i++) {
//			out.addPoint((int) Math.round(union.getX(i)), (int) Math.round(union.getY(i)));
//		}
		
		return union;
	}

	public GameCoordinates getCoordinates() {
		return new GameCoordinates(getX(), getZ());
	}
	
	private int getX() {
		return parseString(x);
	}
	
	private int getZ() {
		return parseString(z);
	}
	
	public int getWidth() {
		return parseString(width);
	}
	
	public int getHeight() {
		return parseString(height);
	}
	
	private int parseString(String s) {
		s = s.replace("CAMERA_WIDTH", ""+Camera.CAMERA_WIDTH);
		s = s.replace("CAMERA_HEIGHT", ""+Camera.CAMERA_HEIGHT);
		s = s.trim();
		s = s.replace(" ", "");
		
		int i = -1;
		
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			
			i = (int) Double.parseDouble(engine.eval(s).toString());
		} catch(ScriptException e) {
			e.printStackTrace();
		}
		
		return i;
	}
}
