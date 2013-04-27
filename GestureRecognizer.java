package net.nexcius.gesturerecognizer;

import net.nexcius.gesturerecognizer.inputypes.Gesture;
import net.nexcius.gesturerecognizer.inputypes.NVec3;
import net.nexcius.gesturerecognizer.inputypes.NVecSequence;
import net.nexcius.gesturerecognizer.support.ArrayListOperations;
import net.nexcius.gesturerecognizer.support.Tuple;

import java.util.ArrayList;


public class GestureRecognizer {
    
    private static final float GRAVITY = 9.81f;
    
    private int averageDelta = 3;
    private float silenceThreshold = 0.2f;
    
    private ArrayList<Gesture> gestures;
    private ArrayList<NVec3> buffer;
    private int bufferSize = 100;
    
    private float gestureThreshold = 0.4f;

    
    public GestureRecognizer() {
        gestures = new ArrayList<Gesture>();
        buffer = new ArrayList<NVec3>();
    }
    
    public GestureRecognizer(int bufferSize) {
        gestures = new ArrayList<Gesture>();
        buffer = new ArrayList<NVec3>();
        this.bufferSize = bufferSize;
    }
    
    
    public void train(String gestureType, String filename) {
        NVecSequence nvs = new NVecSequence(filename);
        train(gestureType, nvs);
    }
    
    
    public void train(String gestureType, NVecSequence nvs) {
        int newBufferMax = (int)((float)nvs.getVectors().size() * 1.5f);
        if(newBufferMax > bufferSize) {
            bufferSize = newBufferMax;
        }
        
        for(Gesture g : gestures) {
            if(g.getGestureType().equalsIgnoreCase(gestureType)) {
                g.train(nvs);
                return;
            }
        }
        
        Gesture g = new Gesture(gestureType);
        g.train(nvs);
        gestures.add(g);
    }
    
    
    private String recognizeVectors(ArrayList<NVec3> vectors) {
        if(vectors.size() < 5) {
            System.out.println("No vectors specified");
            return null;
        }
        
        int toIndex = 0;
        int clearIndex = 0;

        Tuple<Integer, Integer> preSilenceArea = getNextSilenceArea(vectors);
        if(preSilenceArea != null) {
            vectors.subList(preSilenceArea.x, preSilenceArea.y).clear();
        }
        
        Tuple<Integer, Integer> postSilenceArea = getNextSilenceArea(vectors);
        if(postSilenceArea != null) {
            toIndex = postSilenceArea.x;
            clearIndex = postSilenceArea.y;

        } else {
            toIndex = clearIndex = buffer.size();
        }

        ArrayList<NVec3> singleGesture = new ArrayList<NVec3>();
        for(int i = 0; i < toIndex; i++) {
            singleGesture.add(buffer.get(i));
        }

        Tuple<String, Float>[] probability = new Tuple[gestures.size()];
        for(int i = 0; i < gestures.size(); i++) {
//            System.out.println("CHECKING: " + getAsString(singleGesture));
            probability[i] = new Tuple(gestures.get(i).getGestureType(), gestures.get(i).recognize(singleGesture));
            //System.out.println(gestures.get(i).getGestureType() + ": " + gestures.get(i).recognize(singleGesture));
        }
        
        int maxIndex = 0;
        float maxProb = 0.0f;
        
        for(int i = 0; i < probability.length; i++) {
            System.out.println("POSSIBILITY: " + probability[i].x + ": " + probability[i].y);
            if(probability[i].y > maxProb) {
                maxIndex = i;
                maxProb = probability[i].y;
            }
        }

        if(probability[maxIndex].y > gestureThreshold) {
            vectors.subList(0, clearIndex).clear();
            System.out.println("FOUND: " + probability[maxIndex].x + ": " + probability[maxIndex].y);
            return probability[maxIndex].x;
        } else {
            System.out.println("FOUND: none");
        }
        
        return null;
    }

    private String getAsString(ArrayList<NVec3> singleGesture) {
        String str = "";
        for (NVec3 vec : singleGesture) {
            str += "[" + vec.getX() + ", " + vec.getY() + ", " + vec.getZ() + "]\n";
        }
        return str;
    }

    /**
     * Gets the next silence period in a vector collection
     * @param vectors The ArrayList of NVec to check against
     * @return The range <start, end> if found, null if no silence areas are found
     */
    public Tuple<Integer, Integer> getNextSilenceArea(ArrayList<NVec3> vectors) {
        ArrayList<Float> localAverage = new ArrayList<Float>();
        float avgTemp = 0.0f;
        
        Tuple<Integer, Integer> area = new Tuple(-1, -1);
        
        // Parse vectors
        for(int i = 0; i < vectors.size(); i++) {
            localAverage.add(vectors.get(i).getGravitationalMagnitude());
            
            if(localAverage.size() > averageDelta) {
                avgTemp = ArrayListOperations.average(localAverage);
                
                if(area.x < 0 && avgTemp < silenceThreshold) {
                    area.x = i - averageDelta;
                } else if(area.x >= 0 && area.y < 0 && avgTemp > silenceThreshold) {
                    area.y = i - averageDelta;
                    return area;
                } 
                
                localAverage.remove(0);
            }
        }
        
        if(area.x == -1) {
            return null;
        }
        if(area.y == -1) {
            area.y = vectors.size();
        }
        
        return area;
    }

    
    
    public String recognizeFinite(ArrayList<NVec3> vectors) {
        return recognizeVectors(vectors);
    }
    
    public String recognizeFromBuffer() {
        return recognizeVectors(buffer);
    }
    
    public void addNVec(NVec3 v) {
        buffer.add(v);
        while(buffer.size() > bufferSize)
            buffer.remove(0);
    }
    
    
    
    
	
	/** To be Removed **/
	public static void main(String[] args) {
		GestureRecognizer gr = new GestureRecognizer();
		
		gr.train("LEFT", "left.txt");
		gr.train("LEFT", "left2.txt");
		gr.train("LEFT", "left3.txt");
		gr.train("LEFT", "left4.txt");
		gr.train("LEFT", "left5.txt");
		
		gr.train("RIGHT", "right.txt");
		gr.train("RIGHT", "right2.txt");
		gr.train("RIGHT", "right3.txt");
		gr.train("RIGHT", "right4.txt");
		gr.train("RIGHT", "right5.txt");
		
		
		NVecSequence a = new NVecSequence("left4.txt");
		NVecSequence b = new NVecSequence("right2.txt");
		NVecSequence c = new NVecSequence("right3.txt");
		
		
		for(NVec3 n : a.getVectors()) {
		    gr.addNVec(n);
		}
		
		for(NVec3 n : b.getVectors()) {
            gr.addNVec(n);
        }
		
		for(NVec3 n : c.getVectors()) {
            gr.addNVec(n);
        }
		
		
		gr.recognizeFromBuffer();
		gr.recognizeFromBuffer();
		gr.recognizeFromBuffer();
		gr.recognizeFromBuffer();

		

	}
}


