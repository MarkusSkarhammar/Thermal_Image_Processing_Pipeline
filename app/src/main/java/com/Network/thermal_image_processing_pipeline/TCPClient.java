package com.Network.thermal_image_processing_pipeline;

import android.util.Log;


import com.example.thermal_image_processing_pipeline.MainActivity;
import com.log.log;
import com.pipeline.thermal_image_processing_pipeline.RawImageData;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import java.net.Socket;

import java.util.Arrays;

public class TCPClient {

    // useful variables
    private int str_frm_nbr, str_exposure, str_timestamp_sec, str_timestamp_usec, str_format, num_pix, rec_bytes, tot_bytes = 0, lol;
    private double bytes_per_pix = 0;
    private byte[] b, imageData;

    private Socket s;
    private final int SERVER_PORT = 1234;
    private final String SERVER_IP = "192.168.0.90";

    // used to send messages
    private DataOutputStream bufferOut;

    // used to read messages from the server
    private DataInputStream bufferIn;

    public TCPClient(){

    }

    /**
     * Start stream.
     */
    public void StartReadingRawStream(){
        try {

            // Get shutter and gain
            try {
                SSHConnection.getGain("root", "pass", "192.168.0.90");
                SSHConnection.getShutter("root", "pass", "192.168.0.90");
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d("TCP Client", "C: Connecting...");

            s = new Socket(SERVER_IP, SERVER_PORT);

            Log.d("TCP Client", "C: Connected!");

            try {

                // To the server
                bufferOut = new DataOutputStream(s.getOutputStream());

                // From the server
                bufferIn = new DataInputStream( s.getInputStream() );

                long timeStampStart, timeStampEnd;

                int amountRead;

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

                    doHeaderStuff();

                    rec_bytes = 0;
                    imageData = new byte[tot_bytes];
                    b = new byte[66000];
                    while (rec_bytes < tot_bytes){
                        amountRead = bufferIn.read(b);
                        addDataFromArray(imageData, b, rec_bytes, amountRead);
                        rec_bytes += amountRead;
                    }


                    //if(MainActivity.offset14bit == 0.)
                        //getGain(imageData);


                    if(MainActivity.stream.size() < 5){
                        MainActivity.stream.add(new RawImageData(imageData, getGain(imageData)));
                    }

                    // Send server an ack message
                    bufferOut.write(ack);

                    timeStampEnd = System.currentTimeMillis();
                    log.setGetImageDataTime(timeStampEnd-timeStampStart);

                }

                }catch (Exception e) {
                Log.e("TCP", "S: Error", e);
            }

        } catch (IOException e) {
            Log.e("TCP", "C: Error", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param data where the offset for 14-bit conversion is stored.
     */
    private void getOffset(byte[] data){
        int b1, b2;
        int b3;
        b1 = data[3] & 0xff; b2 = data[4] & 0xff;
        b3 = ((b2 << 8) | b1);
        MainActivity.offset14bit = b3;
    }

    /**
     *
     * @param data where the gain for 14-bit conversion is stored.
     */
    private double getGain(byte[] data){
        int b1, b2, b3, b4, a, b, c, d;

        b1 = data[1] & 0xff; b2 = data[2] & 0xff; b3 = data[3] & 0xff; b4 = data[4] & 0xff;

        a = (b1 >> 4) | (b2 << 4);

        b = ((b4 & 0xf) << 8) | b3;

        c = (b << 12);

        d = c + a;

       return d / 16834.0;
    }

    /**
     * Stop the stream;
     */
    public void StopReadingRawStream(){
        try {
            s.close();
            bufferIn.close();
            bufferOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convert byte array to int.
     * @param bytes The data.
     * @return An int.
     */
    private int fromByteArray(byte[] bytes) {
        return bytes[3] << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF);
    }

    /**
     * Process header data from the server.
     */
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

    /**
     * Copy an array into another.
     * @param arrayTo Destination array.
     * @param arrayFrom Source array.
     * @param at Start at position.
     * @param length Length of the data to copy.
     */
    private void addDataFromArray(byte[] arrayTo, byte[] arrayFrom, int at, int length) {
        System.arraycopy(arrayFrom, 0, arrayTo, at, length);
    }

}
