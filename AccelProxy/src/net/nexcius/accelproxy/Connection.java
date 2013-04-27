package net.nexcius.accelproxy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

public class Connection extends AsyncTask<Void, Void, Void> {
	private BufferedReader in = null;
	private PrintWriter out = null;
	private Socket socket = null;
	
	private boolean activeConnection = false;
	private boolean updatedBuffer = false;
	private String sendBuffer = "";
	
	private String host = "";
	private int port = 0; 
	private TextView statusText = null; 
	
	
	
	public Connection(TextView statusText, String host, int port) {
		this.statusText = statusText;
		this.host = host;
		this.port = port;
	}

	
	public boolean isConnected() {
		return activeConnection;
	}
	
	public void disconnect() {
		activeConnection = false;
		//notify();
	}
	
	
	public void send(String message) {
		sendBuffer = message;
		updatedBuffer = true;
		
		synchronized(this) {
			notify();
		}
		
	}
	


	@Override
	protected Void doInBackground(Void... params) {
		try {
			socket = new Socket(host, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			activeConnection = true;
			
		} catch (Exception e) {
			e.printStackTrace();
			socket = null;
			activeConnection = false;
			return null;
		}
		
		Log.d("ACCEL_PROXY", "Connected Session");
		//statusText.setText("Connected");
		
		
		
		while(activeConnection) {
			
			if(!socket.isConnected()) {
				break;
			}
			
			if(updatedBuffer) {
				out.write(sendBuffer + '\n');
				out.flush();
				updatedBuffer = false;
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//e.printStackTrace();
				Log.d("ACCEL_PROXY", "interrupted");
			}
		}
		
		Log.d("ACCEL_PROXY", "Disconnecting Session");
		try {
			out.close();
			in.close();
			socket.close();
		} catch (IOException e) {
			//statusText.setText("Error closing connection");
			e.printStackTrace();
		}
		
		//statusText.setText("Not Connected");
		activeConnection = false;
		
		
		return null;
	}
}
