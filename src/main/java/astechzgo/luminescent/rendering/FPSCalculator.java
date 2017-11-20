package astechzgo.luminescent.rendering;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FPSCalculator {

    private final DecimalFormat formatter; 
    
    private double lastTime = 0;
    
    private final double seconds;
    
    private final Map<Double, Integer> frames;
    
    public FPSCalculator(DecimalFormat formatter, double seconds) {
        this.formatter = formatter;
        this.seconds = seconds;
        frames = new HashMap<>();
    }
    
    public int getFPS(double time) {
        int fps = (int) Math.round(1.0 / (time - lastTime));
        
        lastTime = time;
        
        List<Double> removeList = new ArrayList<>();
        
        for(Double key : frames.keySet()) {
            if(key <= time - seconds) {
                removeList.add(key);
            }
        }
        
        for(Double key : removeList) {
            frames.remove(key);
        }
        
        frames.put(time, fps);
        
        int sum = 0;
        for(Integer e : frames.values()) {
            sum += e;
        }
        
        return (int) ((float)sum / frames.size());
    }
    
    public String getFormattedFPS(double time) {
        return formatter.format(getFPS(time));
    }
}
