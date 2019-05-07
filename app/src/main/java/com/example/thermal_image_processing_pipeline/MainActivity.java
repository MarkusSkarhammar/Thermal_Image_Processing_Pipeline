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

import com.Network.thermal_image_processing_pipeline.TCPClient;
import com.pipeline.thermal_image_processing_pipeline.Pipeline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private PGMImage img = null, img2 = null, shutter = null;
    private Canvas canvas= null;
    private Pipeline pipeline = null;
    private TCPClient tcpClient;
    public static ArrayList<PGMImage> stream = new ArrayList<>();
    public static ArrayList<Bitmap> bitmaps = new ArrayList<>();
    private PGMImage imageTemp;
    private boolean run = true;

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

        //startTCPConnection();
        //new UpdateTask().execute("");
        startUpdateWorker();
        generateBitmaps();
        new ConnectTask().execute("");


    }

    private void init(){
        pipeline = new Pipeline(MainActivity.this, 384, 288);
        ImageView imgView = findViewById(R.id.imageView1);
        /*shutter = FileManagement.readFile(MainActivity.this, "Shutter_off000000");
        pipeline = new Pipeline(MainActivity.this, shutter.getWidth(), shutter.getHeight());
        pipeline.getShutterValues(shutter);

        // Image 1
        img = FileManagement.readFile(MainActivity.this, "Corri_raw000070");

        pipeline.processImage(img);

        ImageView imgView = findViewById(R.id.imageView1);
        img.draw(imgView);*/

        // Setup SeekBars for brightness, contrast and sharpening

        final SeekBar brightness=(SeekBar) findViewById(R.id.seekBar1);
        final SeekBar contrast=(SeekBar) findViewById(R.id.seekBar2);
        final SeekBar sharpening=(SeekBar) findViewById(R.id.seekBar3);

        SeekBarListener seekBarListener = new SeekBarListener(img, imgView);
        brightness.setOnSeekBarChangeListener(seekBarListener);
        contrast.setOnSeekBarChangeListener(seekBarListener);
        sharpening.setOnSeekBarChangeListener(seekBarListener);
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

    public class UpdateTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... message) {
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if(stream != null && stream.size() > 0){
                        imageTemp = stream.remove(0);
                        ImageView imgView = findViewById(R.id.imageView1);
                        imageTemp.draw(imgView);
                    }
                }
            });
            return "";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

        }
    }

    public void startTCPConnection(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //we create a TCPClient object
                tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                    @Override
                    //here the messageReceived method is implemented
                    public void messageReceived(String message) {
                        //this method calls the onProgressUpdate
                    }
                });
                tcpClient.StartReadingRawStream();

            /*//we create a TCPClient object
            tcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override);
            tcpClient.StartReadingRawStream();*/
                Log.d("TCP Client", "Session ended.");
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void startUpdateWorker(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(run){
                    ImageView imgView = findViewById(R.id.imageView1);
                    updateView(imgView);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void updateView(final ImageView imgView){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(bitmaps != null && bitmaps.size() > 0){
                    DisplayHandler.DrawCanvas(bitmaps.remove(0), imgView);
                    //stream.clear();
                }
            }
        });
    }
    private void generateBitmaps(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long timeStampStart, timeStampEnd;
                while(run){
                    timeStampStart = System.currentTimeMillis();
                    if(stream != null && stream.size() > 0){
                        imageTemp = stream.remove(0);
                        if(imageTemp != null){
                            pipeline.processImage(imageTemp);
                            bitmaps.add(imageTemp.getProcessedBitmap());
                        }
                        //stream.clear();
                    }
                    timeStampEnd = System.currentTimeMillis();
                    //Log.d("TCP Client:", " Time to get image: " + (timeStampEnd - timeStampStart) + " ms.");
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }


}
