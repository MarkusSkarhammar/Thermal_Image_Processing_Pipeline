package com.Network.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.health.TimerStat;
import android.util.Log;
import android.widget.TextView;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.MainActivity;
import com.example.thermal_image_processing_pipeline.PGMImage;
import com.example.thermal_image_processing_pipeline.R;
import com.log.log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCPClient {

    // useful variables
    private int str_w = 0,  str_h = 0, str_frm_nbr, str_exposure, str_timestamp_sec, str_timestamp_usec, str_format, num_pix, rec_bytes, tot_bytes = 0, lol;
    private double bytes_per_pix = 0;
    private byte[] b, imageData;
    private Color c = new Color();

    private Socket s;
    private final int SERVER_PORT = 1234;
    private final String SERVER_IP = "192.168.0.90";

    // message to send to the server
    private String serverMessage;

    // used to send messages
    private DataOutputStream bufferOut;

    // used to read messages from the server
    private DataInputStream bufferIn;

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

                bufferIn = new DataInputStream( s.getInputStream() );

                long timeStampStart, timeStampEnd, timeStampStart2, timeStampEnd2, timeStampStart3, timeStampEnd3, timeStampStart4, timeStampEnd4;

                int[][] array2d;

                int[] dataAsInt;

                int amountRead, temp = 0, highest = 0, b1, b2 = 0, b3 = 0, colorValue, dataIndex;

                byte[] message, ack = {0x24, 1, 0, 0, 0};

                byte[][] server_params = {
                        {0x48, 0x1e, 0, 0, 0},
                        {0x73, 0, 0, 0, 0},
                        {0x77, (byte) 0x80, 0x01, 0, 0},
                        {0x68, 0x20, 0x01, 0, 0},
                        {0x78, 0, 0, 0, 0},
                        {0x79, 0, 0, 0, 0},
                        {0x65, 0x02, 0, 0, 0}
                };

                while (!s.isClosed()) {

                    timeStampStart = System.currentTimeMillis();

                    for(int i = 0; i < server_params.length; i++){
                        message = server_params[i];
                        bufferOut.write(message);
                    }

                    //timeStampStart4 = System.currentTimeMillis();

                    doHeaderStuff();

                    //timeStampEnd4 = System.currentTimeMillis();
                    //Log.d("TCP Client:", " Time to do header stuff: " + (timeStampEnd4 - timeStampStart4) + " ms.");

                    //timeStampStart3 = System.currentTimeMillis();
                    //amountRead = 0;
                    rec_bytes = 0;
                    imageData = new byte[tot_bytes];
                    b = new byte[66000];
                    while (rec_bytes < tot_bytes){
                        amountRead = bufferIn.read(b);
                        addDataFromArray(imageData, b, rec_bytes, amountRead);
                        rec_bytes += amountRead;
                    }
                    //timeStampEnd3 = System.currentTimeMillis();
                    //log.addInput(" Time to get image data: " + (timeStampEnd3 - timeStampStart3) + " ms.");

                    //timeStampStart2 = System.currentTimeMillis();

                    //array2d = new int[str_h][str_w];
                    /*dataAsInt = new int[str_h*str_w];
                    temp = 0; highest = 0; b2 = 0; b3 = 0;
                    dataIndex = 0;
                    for(int h=0; h<str_h;h++)
                        for(int w=0;w<str_w;w++){

                            if(dataIndex % 3 == 0){
                                b1 = imageData[dataIndex] & 0xff; b2 = imageData[(dataIndex)+1] & 0xff; b3 = imageData[(dataIndex)+2] & 0xff;
                                temp = ((b2 & 0xf) << 8) | b1;
                                dataIndex += 1;
                            }else{
                                temp = (b2 >> 4) | (b3 << 4);
                                dataIndex += 2;
                            }
                            colorValue = (int)(((double)temp / 4095.0) * 255);
                            //array2d[h][w] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
                            dataAsInt[(h*str_w) + w] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
                            //if(temp > highest)
                                //highest = temp;
                        }*/

                    //timeStampEnd2 = System.currentTimeMillis();
                    //log.addInput(" Time to process image data: " + (timeStampEnd2 - timeStampStart2) + " ms.");

                    if(MainActivity.stream.size() < 5){
                        //PGMImage img = new PGMImage(dataAsInt);
                        MainActivity.stream.add(imageData);
                    }

                    // Send server an ack message
                    bufferOut.write(ack);

                    timeStampEnd = System.currentTimeMillis();
                    log.addInput(" Time to get image: " + (timeStampEnd - timeStampStart) + " ms.");
                    //Log.d("TCP Client:", " Time to get image: " + (timeStampEnd - timeStampStart) + " ms.");


                }
                //Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

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

    private int fromByteArray(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }

    private void doHeaderStuff() throws Exception {
        b = new byte[5];

        bufferIn.read(b);
        char m = (char)b[0];

        if(m != 'H')
            throw new Exception("Invalid command. Expected H, got " + m + ".");
        int header_length = fromByteArray(Arrays.copyOfRange(b, 1, b.length));

        byte[] data;

        while(header_length > 0){
            bufferIn.read(b);
            m = (char)b[0];
            data = Arrays.copyOfRange(b, 1, b.length);
            switch (m){
                case 'w':
                    MainActivity.str_w = fromByteArray(data);
                    break;
                case 'h':
                    MainActivity.str_h = fromByteArray(data);
                    break;
                case 's':
                    str_frm_nbr = fromByteArray(data);
                    break;
                case 'f':
                    str_format = fromByteArray(data);
                    if (str_format == 0)
                        bytes_per_pix = 2;
                    else if(str_format == 1)
                        bytes_per_pix = 1.5;
                    break;
                case 'e':
                    str_exposure = fromByteArray(data);
                    break;
                case 'S':
                    str_timestamp_sec = fromByteArray(data);
                    break;
                case 'U':
                    str_timestamp_usec = fromByteArray(data);
                    break;
                case 'i':
                    break;
                case '+':
                    tot_bytes = fromByteArray(data);
                    break;
                case 'M':
                    lol = fromByteArray(data);
                    break;
                default:
                    throw new Exception("Unknown parameter in header. m = " + m);
            }
            header_length -= b.length;
        }

        num_pix = (int) (MainActivity.str_w * MainActivity.str_h * bytes_per_pix);
        if (tot_bytes != num_pix)
            throw new Exception("ERROR: tot_bytes != num_pix");
    }

    private void addDataFromArray(byte[] arrayTo, byte[] arrayFrom, int at, int length) {
        System.arraycopy(arrayFrom, 0, arrayTo, at, length);
    }

}
