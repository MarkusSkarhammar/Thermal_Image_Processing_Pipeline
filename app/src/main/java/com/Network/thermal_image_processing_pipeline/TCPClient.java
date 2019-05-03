package com.Network.thermal_image_processing_pipeline;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.thermal_image_processing_pipeline.DisplayHandler;
import com.example.thermal_image_processing_pipeline.PGMImage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.InetAddress;
import java.net.Socket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TCPClient {

    // useful variables
    int str_w = 0,  str_h = 0, str_frm_nbr, str_exposure, str_timestamp_sec, str_timestamp_usec, str_format, num_pix, rec_bytes, tot_bytes = 0, lol;
    double bytes_per_pix = 0;
    byte[] b;
    ArrayList<Byte> tempImageData = new ArrayList<>();

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

                if(s.isConnected()){
                    byte[][] server_params = {
                            {0x48, 0x1e, 0, 0, 0},
                            {0x73, 0, 0, 0, 0},
                            {0x77, (byte) 0x80, 0x01, 0, 0},
                            {0x68, 0x20, 0x01, 0, 0},
                            {0x78, 0, 0, 0, 0},
                            {0x79, 0, 0, 0, 0},
                            {0x65, 0x02, 0, 0, 0}
                    };
                    for(int i = 0; i < server_params.length; i++){
                        byte[] message = server_params[i];
                        Log.d("TCP Client", "Sending: [" + message[0] + ", " + message[1] + ", " + message[2] + ", " + message[3] + ", " + message[4] + "]");
                        //bufferOut.writeInt(message[0]);
                        //bufferOut.write(message.length);
                       bufferOut.write(message);
                        //bufferOut.flush();
                    }
                }

                doHeaderStuff();

                while (rec_bytes < tot_bytes){
                    b = new byte[tot_bytes - rec_bytes];
                    bufferIn.read(b);
                    rec_bytes += b.length;
                    tempImageData.addAll(toList(b));
                }

                int[][] array2d = new int[str_h][str_w];
                int temp, highest = 0;
                for(int h=0; h<str_h;h++)
                    for(int w=0;w<str_w;w++){
                        temp = tempImageData.get((h*str_w) + w);
                        array2d[h][w] = temp;
                        if(temp > highest)
                            highest = temp;
                    }

                PGMImage img = new PGMImage(array2d, highest);
                Bitmap bitmap = DisplayHandler.generateBitmapFromPGM(img);

                while (!s.isClosed()) {

                    serverMessage = "" + bufferIn.read(b);

                    int i = b[65537];
                    if (!serverMessage.contains("-1")) {
                        Log.d("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");
                        //call the method messageReceived from MyActivity class
                        //messageListener.messageReceived(serverMessage);
                    }
                    serverMessage = "";
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
                    str_w = fromByteArray(data);
                    break;
                case 'h':
                    str_h = fromByteArray(data);
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

        num_pix = (int) (str_w * str_h * bytes_per_pix);
        if (tot_bytes != num_pix)
            throw new Exception("ERROR: tot_bytes != num_pix");
    }

    private static List<Byte> toList(byte[] array) {
        if (array==null) {
            return new ArrayList(0);
        } else {
            int size = array.length;
            List<Byte> list = new ArrayList(size);
            for(int i = 0; i < size; i++) {
                list.add(array[i]);
            }
            return list;
        }
    }

}
