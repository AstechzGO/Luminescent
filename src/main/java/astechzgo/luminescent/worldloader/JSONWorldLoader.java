package astechzgo.luminescent.worldloader;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.MalformedInputException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.rendering.Camera;

public class JSONWorldLoader {

	private static final EquationInterpreter interpreter = new EquationInterpreter(Map.of("CAMERA_WIDTH", Camera.CAMERA_WIDTH, "CAMERA_HEIGHT", Camera.CAMERA_HEIGHT));

	private String x;
	private String z;
	
	public String width;
	public String height;

	public static List<Room> loadRooms() {
		Gson g = new Gson();
		StringBuilder parse = new StringBuilder();
		
		InputStream in = null;
		
        try {
            in = getResourceAsURL("world/DefaultWorld.json").openStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		String line = "";
		try {
			while((line = input.readLine()) != null) {
				parse.append(line);
			}	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONWorldLoader[] loaders = g.fromJson(parse.toString(), JSONWorldLoader[].class);
		List<Room> rooms = new ArrayList<>();
		
		for(JSONWorldLoader loader : loaders) {
			rooms.add(new Room(loader));
		}
		
		return rooms;
	}
	
	public GameCoordinates getCoordinates() {
		return new GameCoordinates(getX(), getZ());
	}
	
	private double getX() {
		return parseString(x);
	}
	
	private double getZ() {
		return parseString(z);
	}
	
	public double getWidth() {
		return parseString(width);
	}
	
	public double getHeight() {
		return parseString(height);
	}
	
	private double parseString(String s) {
		double i = -1;

		try {
			i = interpreter.resolve(s);
		} catch(MalformedInputException e) {
			e.printStackTrace();
		}
		
		return i;
	}
}
