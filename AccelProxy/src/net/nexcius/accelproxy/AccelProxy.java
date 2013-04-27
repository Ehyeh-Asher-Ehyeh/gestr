package net.nexcius.accelproxy;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AccelProxy extends Activity implements OnClickListener {
    private EditText input = null;
    private Button connButton = null;
    private TextView status = null;

    private Intent serviceIntent = null;
    private boolean connected = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        input = (EditText) findViewById(R.id.input);
        connButton = (Button) findViewById(R.id.conn_button);
        status = (TextView) findViewById(R.id.status_text);
                
        connButton.setOnClickListener(this);
    }

        
    @Override
    public void onClick(View v) {
            if(v == connButton) {
            if(!connected) {
                status.setText("Connecting to " + input.getText());
                connButton.setText("Disconnect");

                serviceIntent = new Intent(this, AccelService.class);
                serviceIntent.putExtra("SERVER_IP", input.getText().toString());
                startService(serviceIntent);

                connected = true;
            } else {
                stopService(serviceIntent);

                status.setText("Disconnected");
                connButton.setText("Connect");
                connected = false;
            }
        }

    }



/*
    private static final int PORT = 21345;

    private TextView statusText = null;
    private EditText addressBox = null;
    private Button connectButton = null;

    private Connection conn = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accel_proxy);

        statusText = (TextView) findViewById(R.id.status_text);
        addressBox = (EditText) findViewById(R.id.address_box);
        connectButton = (Button) findViewById(R.id.conn_button);
        connectButton.setOnClickListener(this);

        statusText.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        if(v == connectButton) {
            if(conn == null) {
                conn = new Connection(statusText, addressBox.getText().toString(), PORT);
                conn.execute();


            } else {
                conn.disconnect();
                conn = null;
            }
        } else {
            Log.d("ACCEL_PROXY", "PRESSED");
            if(conn != null && conn.isConnected()) {
                conn.send("YO!");
            }
        }
    }
*/
}
