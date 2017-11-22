package astechzgo.luminescent.worldloader;

import static astechzgo.luminescent.utils.SystemUtils.getResourceAsURL;

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

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.gameobject.Room;
import astechzgo.luminescent.rendering.Camera;

public class JSONWorldLoader {
	
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
