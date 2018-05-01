package astechzgo.luminescent.rendering;

import astechzgo.luminescent.coordinates.GameCoordinates;
import astechzgo.luminescent.coordinates.ScaledWindowCoordinates;
import astechzgo.luminescent.utils.DisplayUtils;

public class LightSource {
    public static final int LIGHTS = 250;

    private static final LightSource[] lights = new LightSource[LIGHTS];

    private static int searchFirst(LightSource source) {
        for(int i = 0; i < lights.length; i++) {
            if(lights[i] == source) {
                return i;
            }
        }

        return -1;
    }

    public static boolean contains(LightSource source) {
        return searchFirst(source) != -1;
    }

    public static void addSource(LightSource source) {
        if(source == null) {
            return;
        }

        if(!contains(source)) {
            int i = searchFirst(null);
            if(i == -1) {
                System.out.println("Out of space for lights");
                return;
            }

            lights[i] = source;
        }
    }

    public static void removeSource(LightSource source) {
        if(source == null) {
            return;
        }

        int i = searchFirst(source);
        if(i != -1) {
            lights[searchFirst(source)] = null;
        }
    }

    public static LightSource get(int index) {
        return lights[index];
    }

    private GameCoordinates coords;
    private double radius;

    public LightSource(GameCoordinates coords, double radius) {
        this.coords = coords;
        this.radius = radius;
    }

    public GameCoordinates getCoords() {
        return coords;
    }

    public void setCoords(GameCoordinates coords) {
        this.coords = coords;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public float getScaledX() {
        return DisplayUtils.widthOffset + (float) new ScaledWindowCoordinates(coords).getScaledWindowCoordinatesX();
    }

    public float getScaledY() {
        return DisplayUtils.heightOffset + (float) new ScaledWindowCoordinates(coords).getScaledWindowCoordinatesY();
    }

    public float getScaledRadius() {
        return (float) Math.round((double) radius / Camera.CAMERA_WIDTH * (DisplayUtils.getDisplayWidth() - DisplayUtils.widthOffset * 2));
    }
}
