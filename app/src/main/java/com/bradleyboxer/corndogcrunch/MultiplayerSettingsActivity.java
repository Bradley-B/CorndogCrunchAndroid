package com.bradleyboxer.corndogcrunch;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bradleyboxer.corndogcrunch.highscores.Score;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MultiplayerSettingsActivity extends AppCompatActivity {

    PrintWriter out = null;
    BufferedReader in = null;
    Socket socket = null;
    int textProgress = 0;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiplayer_settings);
    }

    public void onConnectButton(View v) {
        String name = ((EditText)findViewById(R.id.multiplayerName)).getText().toString();
        String ip = ((EditText)findViewById(R.id.editIp)).getText().toString();
        String port = ((EditText)findViewById(R.id.editPort)).getText().toString();

        TextView multiplayerDisplay = ((TextView)findViewById(R.id.multiplayerDisplay));
        if(validateIp(ip) && validatePort(port) && name.length()>0) {
            try {
                setButtonsConnected(true);
                connectToServer(InetAddress.getByName(ip), name, Integer.valueOf(port));
            } catch (IOException e) { //NOTE: THIS CATCH ONLY HANDLES THE InetAddress EXCEPTION- NOT THE SOCKET ITSELF!
                setButtonsConnected(false);
                multiplayerDisplay.setText("Unable to resolve address. Check the IP and try again.");
            }
        } else {
            multiplayerDisplay.setText("Preference values are invalid. Please correct errors and try again.");
            setButtonsConnected(false);
        }
    }

    public void onSendChat(View v) {
        sendMessageToServer(((EditText)findViewById(R.id.sendChatText)).getText().toString());
    }

    public void onReadyButton(View v) {
        sendMessageToServer("/ready");
        findViewById(R.id.multiplayerReadyButton).setEnabled(false);
        findViewById(R.id.multiplayerUnreadyButton).setEnabled(true);
    }

    public void onUnreadyButton(View v) {
        onUnreadyButton();
    }

    public void onUnreadyButton() {
        sendMessageToServer("/unready");
        findViewById(R.id.multiplayerReadyButton).setEnabled(true);
        findViewById(R.id.multiplayerUnreadyButton).setEnabled(false);
    }

    public void connectToServer(final InetAddress ip, final String name, final int port) throws IOException {

        Thread connectThread = new Thread(new Runnable() { //connect to server
            @Override
            public void run() {
                try {
                    socket = new Socket(ip, port);

                    out = new PrintWriter(socket.getOutputStream());
                    sendMessageToServer("/nameReport "+name);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((TextView) findViewById(R.id.multiplayerDisplay)).setText("Connected to "+socket.getRemoteSocketAddress());
                        }
                    });

                    Thread listenerThread = new Thread(new Runnable() { //listen from server for commands
                        @Override
                        public void run() {
                            try {
                                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            } catch (IOException e) {}

                            while(socket.isConnected() && !socket.isClosed()) {
                                try {
                                    final String line = in.readLine();
                                    if(socket.isConnected()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                processIncomingCommand(line);
                                            }
                                        });
                                    }
                                } catch (IOException e) {}
                            }
                        }
                    });
                    listenerThread.start();

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonsConnected(false);
                        }});
                }
            }
        });
        connectThread.start();

        Log.i("info", "Trying connection to: "+ip.getHostAddress());
    }

    public void sendMessageToServer(final String message) {
        Thread senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    out.println(message);
                    out.flush();
                } catch(NullPointerException e) {}
            }
        });
        senderThread.start();
    }

    public void processIncomingCommand(String command) {
        String basecommand = Util.getCommand(command);
        //String subcommand = Util.getSubcommand(command);

        TextView multiplayerDisplay = ((TextView)findViewById(R.id.multiplayerDisplay));

        if(basecommand.equals("start")) { //start the game
            onUnreadyButton();

            Intent intent = new Intent(this, MultiplayerActivity.class);
            intent.putExtra("startTime", System.currentTimeMillis()+1000);
            startActivityForResult(intent, 1);

        } else if(Util.extractNumber(basecommand)>10) { //check contains score report
            multiplayerDisplay.setText(basecommand);
            textProgress = 3;
        } else if(basecommand.contains("clear")) {
            multiplayerDisplay.setText("");
        } else {
            if(textProgress>=15) {
                multiplayerDisplay.setText(command);
                textProgress = 0;
            } else {
                multiplayerDisplay.setText(command+"\n"+multiplayerDisplay.getText().toString());
                textProgress++;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.activity_multiplayer_settings);
        setButtonsConnected(connected);
        ((TextView)findViewById(R.id.multiplayerDisplay)).setText("");
        textProgress = 0;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int score = data.getIntExtra("scoreReport", 0);
        Log.i("MULTIPLAYER", "client score reported as "+score);
        sendMessageToServer("/scoreReport "+score);
    }

    @Override
    public void onBackPressed() {
        closeConnection();
        super.onBackPressed();
    }

    public void setButtonsConnected(boolean isConnected) {
        connected = isConnected;
        findViewById(R.id.multiplayerName).setEnabled(!isConnected);
        findViewById(R.id.editPort).setEnabled(!isConnected);
        findViewById(R.id.editIp).setEnabled(!isConnected);
        findViewById(R.id.connectButton).setEnabled(!isConnected);

        findViewById(R.id.multiplayerReadyButton).setEnabled(isConnected);
        findViewById(R.id.sendChatButton).setEnabled(isConnected);
    }

    public void closeConnection() {
        connected = false;
        try {
            Log.i("MULTIPLAYER", "disconnecting from server");
            sendMessageToServer("/disconnect");
            out.close();
            in.close();
            socket.close();
            textProgress = 0;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException ex) {}
    }

    public boolean validateIp(String ip) {
        return Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$").matcher(ip).matches();
    }

    public boolean validatePort(String port) {
        try {
            int integerPort = Integer.decode(port);
            if(integerPort<65535 && integerPort>0) {
                return true;
            }
        } catch (NumberFormatException e) {}
        return false;
    }
}
