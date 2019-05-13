package com.example.thermal_image_processing_pipeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.Network.thermal_image_processing_pipeline.TCPClient;
import com.log.log;
import com.pipeline.thermal_image_processing_pipeline.Pipeline;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private PGMImage img = null, img2 = null, shutter = null;
    private Canvas canvas= null;
    private Pipeline pipeline = null;
    private TCPClient tcpClient;
    public static ArrayList<PGMImage> stream = new ArrayList<>();
    public static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private PGMImage imageTemp, imageTemp2;
    private boolean run = true;
    private AtomicBoolean streamLock = new AtomicBoolean();

    public static int brightness = 0;
    public static double contrast = 1.0;
    public static int sharpening = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.loadLibrary("opencv_java4");

        // Check if we have read permission
        //for(int i = 0; i < PERMISSIONS_STORAGE.length; i++){
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
            //}
        }

        setContentView(R.layout.activity_main);
        init();

        generateBitmaps();
        new ConnectTask().execute("");


    }

    private void init(){
        pipeline = new Pipeline(MainActivity.this, 384, 288);
        ImageView imgView = findViewById(R.id.imageView1);

        final SeekBar brightness=(SeekBar) findViewById(R.id.seekBar1);
        final SeekBar contrast=(SeekBar) findViewById(R.id.seekBar2);
        final SeekBar sharpening=(SeekBar) findViewById(R.id.seekBar3);

        SeekBarListener seekBarListener = new SeekBarListener(img, imgView);
        brightness.setOnSeekBarChangeListener(seekBarListener);
        contrast.setOnSeekBarChangeListener(seekBarListener);
        sharpening.setOnSeekBarChangeListener(seekBarListener);

        streamLock.set(false);

        TextView txtView = findViewById(R.id.textView3);
        log.setTextView(txtView);
        log.setActivity(MainActivity.this);
    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object
            tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            tcpClient.StartReadingRawStream();

            /*//we create a TCPClient object
            tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override);
            tcpClient.StartReadingRawStream();*/
            Log.d("TCP Client", "Session ended.");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            imageTemp = stream.remove(0);
            ImageView imgView = findViewById(R.id.imageView1);
            imageTemp.draw(imgView);
        }
    }

    private void updateView(final ImageView imgView){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(stream != null && stream.size() > 0){
                    if(streamLock.compareAndSet(false, true)){
                        imageTemp2 = stream.get(0);
                        if(imageTemp2.getProcessedBitmap() != null){
                            long timeStampStart, timeStampEnd;
                            timeStampStart = System.currentTimeMillis();

                            DisplayHandler.DrawCanvas(imageTemp2.getProcessedBitmap(), imgView);
                            stream.remove(0);

                            timeStampEnd = System.currentTimeMillis();
                            Log.d("Pipeline:", " Time to process image: " + (timeStampEnd - timeStampStart) + " ms.");
                            //txtView.setText("Time to process image: " + (timeStampEnd - timeStampStart) + " ms.");

                        }
                        streamLock.set(false);
                    }
                }
            }
        });
    }

    private void generateBitmaps(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long timeStampStart, timeStampEnd;
                ImageView imgView = findViewById(R.id.imageView1);
                while(run){
                    if(stream != null && stream.size() > 0){

                        if(streamLock.compareAndSet(false, true)){
                            if(stream.size() > 0){
                                imageTemp = stream.get(0);
                                if(imageTemp != null && imageTemp.getProcessedBitmap() == null){
                                    //timeStampStart = System.currentTimeMillis();
                                    log.addInput3("Amount in stream: " + stream.size());
                                    pipeline.processImage(imageTemp);
                                    //timeStampEnd = System.currentTimeMillis();
                                    //log.addInput2(" Time to process image: " + (timeStampEnd - timeStampStart) + " ms.");
                                }
                            }
                            streamLock.set(false);
                        }
                        if(stream.size() > 0)
                            updateView(imgView);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }


}
