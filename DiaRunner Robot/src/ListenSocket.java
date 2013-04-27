import net.nexcius.gesturerecognizer.GestureRecognizer;
import net.nexcius.gesturerecognizer.inputypes.NVec3;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;


public class ListenSocket {
    private static final int PORT = 8889;
    private final GestureRecognizer recognizer;
    private BufferedReader in = null;
    private InputEmulator inputEmulator;
    private int fileCount = 0;

    public ListenSocket() {
        inputEmulator = new InputEmulator();
        recognizer = new GestureRecognizer();
        trainRecognizerForRightAndLeftGestures(recognizer);
    }

    public void openConnection() {
        Socket socket = null;
        ServerSocket serverSocket = null;
        fileCount++;
        try {
            serverSocket = new ServerSocket(PORT);

            System.out.println("Waiting for connection on port " + PORT);
            socket = serverSocket.accept();
            System.out.println("Connection received from " + socket.getInetAddress().getHostName());
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Ready to receive incoming data");

            receive();
        } catch (UnknownHostException e) {
            logError("Unknown host while receiving data", e);
        } catch (IOException e) {
            logError("IOException while receiving data", e);
        } finally {
            try {
                if (socket != null)
                    socket.close();
                if (serverSocket != null)
                    serverSocket.close();
                if (in != null)
                    in.close();
            } catch (IOException e) {
                logError("IOException while closing connection", e);
            }
        }
    }

    private void trainRecognizerForRightAndLeftGestures(GestureRecognizer rec) {
        train(rec, GESTURE_TYPE.RIGHT, "right");
        train(rec, GESTURE_TYPE.LEFT, "left");
    }

    private void train(GestureRecognizer rec, GESTURE_TYPE type, String fileBase) {
        List<String> files = new ArrayList<String>();
        while (files.size() < 5)
            files.add(fileBase + (files.size() + 1) + ".txt");
        for (String f : files)
            rec.train(type.id, f);
    }
        
    private void receive() {
        System.out.println("Started listening for incoming traffic.");
        String input = "";
        int vecCount = 0;
        PrintWriter out = null;
//        try {
//            out = new PrintWriter(new FileWriter("C:\\Users\\ajacobsen\\Documents\\Workspace\\master-thesis\\Code\\DiaRunner Robot\\nexus_right"+ fileCount +".txt"));
//        } catch (IOException e) {
//            System.out.println("Couldn't open file: ");
//            e.printStackTrace();
//            out = null;
//        }
        do {
            try {
                input = in.readLine();
                if(input == null)
                    continue;
                float[] parsedInput = getMovementVector(input);
//                System.out.println(String.format("%f,%f,%f", parsedInput[0], parsedInput[1], parsedInput[2]));
//                if (out != null)
//                    out.println(String.format("0:%f,%f,%f", parsedInput[0], parsedInput[1], parsedInput[2]));
                recognizer.addNVec(new NVec3(parsedInput[0], parsedInput[1], parsedInput[2]));
                if (++vecCount > 18) {
                    vecCount = 0;
                    String event = recognizer.recognizeFromBuffer();
                    inputEmulator.registerInput(getAction(event));
                    inputEmulator.handleInput();
                }
            } catch (IOException e) {
                logError("IOException: ", e);
            }
        } while (input != null);
        if (out != null)
            out.close();
        System.out.println("Closed connection");
    }

    private Action getAction(String event) {
        if (areEqual(event, GESTURE_TYPE.LEFT.id))
            return new Action(KeyEvent.VK_SPACE);
        else if (areEqual(event, GESTURE_TYPE.RIGHT.id))
            return null;//new Action(KeyEvent.VK_N);
        return null;
    }

    private boolean areEqual(String a, String b) {
        return a != null && b != null && a.equals(b);
    }

    private float[] getMovementVector(String input) {
        input = input.replaceFirst("\\d+:", "");
        String[] vec = input.split(",");
        float[] floatVec = new float[3];
        for (int i = 0; i < 3; i++)
            floatVec[i] = Float.parseFloat(vec[i]);
        return floatVec;
    }

    private void logError(String s, IOException e) {
        System.out.println(s);
        e.printStackTrace();
    }

    private enum GESTURE_TYPE {
        LEFT("left"), RIGHT("right");

        private final String id;
        private GESTURE_TYPE(String id) {
            this.id = id;
        }
    }
}
