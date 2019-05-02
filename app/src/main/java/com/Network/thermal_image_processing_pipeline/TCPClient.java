package com.Network.thermal_image_processing_pipeline;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {
    private Socket s;
    private final int SERVER_PORT = 1234;
    private final String SERVER_IP = "192.168.0.90";

    // message to send to the server
    private String serverMessage;

    // used to send messages
    private DataOutputStream bufferOut;

    // used to read messages from the server
    private BufferedReader bufferIn;

    // sends message received notifications
    private OnMessageReceived messageListener = null;

    public TCPClient(OnMessageReceived listener){
        messageListener = listener;
    }

    public void StartReadingRawStream(){
        try {

            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVER_IP);

            Log.d("TCP Client", "C: Connecting...");

            s = new Socket(SERVER_IP, SERVER_PORT);

            Log.d("TCP Client", "C: Connected!");

            try {

                //sends the message to the server
                //bufferOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                bufferOut = new DataOutputStream(s.getOutputStream());


                bufferIn = new BufferedReader( new InputStreamReader(s.getInputStream()) );

                if(s.isConnected()){
                    byte[] message = new byte[1];
                    message[0] = 23;
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.writeInt(message.length);
                    bufferOut.write(message);
                    //bufferOut.flush();
                }/*
                if(s.isConnected()){
                    String message = "s";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    //bufferOut.flush();
                }
                if(s.isConnected()){
                    String message = "w";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    //bufferOut.flush();
                }
                if(s.isConnected()){
                    String message = "h";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    //bufferOut.flush();
                }
                if(s.isConnected()){
                    String message = "x";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    //bufferOut.flush();
                }
                if(s.isConnected()){
                    String message = "y";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    //bufferOut.flush();
                }
                if(s.isConnected()){
                    String message = "e";
                    Log.d("TCP Client", "Sending: " + message);
                    bufferOut.println(message);
                    bufferOut.flush();
                }*/


                while (!s.isClosed()) {

                    serverMessage = bufferIn.readLine();
                    Log.d("TCP Client", "This should work.");
                    if (serverMessage != null && messageListener != null) {
                        //call the method messageReceived from MyActivity class
                        messageListener.messageReceived(serverMessage);
                    }
                    serverMessage = "";
                }
                Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

                }catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }

        } catch (IOException e) {
            Log.e("TCP", "C: Error", e);
        }
    }

    /**
     * Sends the message entered by client to the server
     *
     * @param message text entered by client
     */
    public void sendMessage(final String message) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                /*if (bufferOut != null) {
                    Log.d("", "Sending: " + message);
                    bufferOut.println(message);
                    bufferOut.flush();
                }*/
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void StopReadingRawStream(){
        try {
            s.close();
            bufferIn.close();
            bufferOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the Activity
    //class at on AsyncTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

}
