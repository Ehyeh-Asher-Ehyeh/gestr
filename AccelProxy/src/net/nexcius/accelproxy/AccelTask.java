package net.nexcius.accelproxy;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class AccelTask extends AsyncTask<Void, Void, Void> {

    public static final int PORT = 8889;
    private String ip;
    private boolean running = true;


    public String action = "NONE";

    private AccelService handler;

    public AccelTask(String ip, AccelService handler) {
        this.ip = ip;
        running = true;
        action = "NONE";

        this.handler = handler;
    }


    @Override
    protected Void doInBackground(Void... params) {
        try {
            Socket socket = new Socket(ip, PORT);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            while(!isCancelled())
                postSensorChanges(out);
            out.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } 
        return null;
    }

    private void postSensorChanges(PrintWriter out) {
        if(handler.changed) {
            out.println(System.currentTimeMillis() + ":" + handler.accelX + "," + handler.accelY + "," + handler.accelZ);
            handler.changed = false;
        }
    }
}
