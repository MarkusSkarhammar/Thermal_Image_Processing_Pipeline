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
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionS;
import com.pipeline.thermal_image_processing_pipeline.Pipeline;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private PGMImage img = null;
    private Pipeline pipeline = null;
    private TCPClient tcpClient;
    public static ArrayList<byte[]> stream = new ArrayList<>();
    private ArrayList<PGMImage> imageStream = new ArrayList<>();
    private ArrayList<SubArray> subArrays = new ArrayList<>();
    private PGMImage imageTemp;
    private boolean run = true;
    private AtomicBoolean streamLock = new AtomicBoolean();

    public static int brightness = 0;
    public static double contrast = 1.0;
    public static int sharpening = 0;

    public static int str_w = 0,  str_h = 0;

    private int[] dataAsInt;

    final int AMOUNT_OF_THREADS_FOR_CONVERSION = 3;

    private boolean isAlive = true;


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
                    publishProgress(message);
                }
            });
            tcpClient.StartReadingRawStream();

            Log.d("TCP Client", "Session ended.");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    private void updateView(final ImageView imgView){
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(imageStream != null && imageStream.size() > 0){
                    imageTemp = imageStream.get(0);
                    if(imageTemp.getProcessedBitmap() != null){
                        DisplayHandler.DrawCanvas(imageTemp.getProcessedBitmap(), imgView);
                        imageStream.remove(0);
                        log.setAmountInStream(stream.size());
                        log.writeToOutputs();
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

                MotionDetectionS md = new MotionDetectionS();

                while(run){
                    if(stream != null && stream.size() > 0){
                        if(stream.size() > 0){
                            timeStampStart = System.currentTimeMillis();
                            imageTemp = new PGMImage(generateColorsFromImageBytes(stream.remove(0)));
                            pipeline.processImage(imageTemp);
                            imageStream.add(imageTemp);
                            timeStampEnd = System.currentTimeMillis();
                            log.setProcessImageDataTime(timeStampEnd-timeStampStart);

                            md.detect(imageTemp);

                        }
                        if(imageStream.size() > 0)
                            updateView(imgView);
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("PIPELINE_THREAD");
        thread.start();
    }

    private int[] generateColorsFromImageBytes(final byte[] imageData){
        dataAsInt = new int[str_h*str_w];
        for(int i = 0; i < AMOUNT_OF_THREADS_FOR_CONVERSION-1; i++){
            subArrays.add(new SubArray(generateColorsFromImagesBytesWithinRange(
                    imageData,
                    0,
                    0 + (str_h/AMOUNT_OF_THREADS_FOR_CONVERSION) * i,
                    str_w,
                    (str_h/AMOUNT_OF_THREADS_FOR_CONVERSION) * (i + 1),
                    i)
            ));
        }

        for(SubArray sb : subArrays)
            sb.getT().start();

        subArrays.add(new SubArray());
        generateColorsFromImageBytes(
                imageData,
                0,
                0 + (str_h/AMOUNT_OF_THREADS_FOR_CONVERSION) * (AMOUNT_OF_THREADS_FOR_CONVERSION-1),
                str_w,
                str_h,
                AMOUNT_OF_THREADS_FOR_CONVERSION-1
        );

        /*try {
            for(SubArray sb : subArrays){
                if(sb.getT() != null)
                    sb.getT().join();
                addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

         while(isAlive){
            isAlive = false;
            for(SubArray sb : subArrays){
                if(sb.getT() != null){
                    if(!sb.getT().isAlive())
                        addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength());
                    else
                        isAlive = true;
                }else
                    addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength());
            }
        }
        isAlive = true;

        subArrays.clear();
        return dataAsInt;
    }

    private Thread generateColorsFromImagesBytesWithinRange(final byte[] imageData, final int wFrom, final int hFrom, final int wTo, final int hTo, final int threadName){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                generateColorsFromImageBytes(imageData, wFrom, hFrom, wTo, hTo, threadName);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("" + threadName);
        return thread;
    }

    private void generateColorsFromImageBytes(final byte[] imageData, int wFrom, int hFrom, int wTo, int hTo, final int pos){
        SubArray sbTemp;
        int length = (wTo-wFrom)*(hTo-hFrom);
        int[] tempData = new int[length];
        int temp, b1, b2 = 0, b3 = 0, colorValue;
        int dataIndex = (int)(hFrom*str_w*1.5);
        for(int h=hFrom; h<hTo;h++)
            for(int w=wFrom;w<wTo;w++){

                if(dataIndex % 3 == 0){
                    b1 = imageData[dataIndex] & 0xff; b2 = imageData[(dataIndex)+1] & 0xff; b3 = imageData[(dataIndex)+2] & 0xff;
                    temp = ((b2 & 0xf) << 8) | b1;
                    dataIndex += 1;
                }else{
                    temp = (b2 >> 4) | (b3 << 4);
                    dataIndex += 2;
                }
                colorValue = (int)(((double)temp / 4095.0) * 255);
                tempData[((h-hFrom)*str_w) + (w-wFrom)] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
            }
        sbTemp = subArrays.get(pos);
        sbTemp.setAll(tempData,(hFrom*str_w), length );
    }

    private void addDataFromArray(int[] arrayTo, int[] arrayFrom, int at, int length) {
        System.arraycopy(arrayFrom, 0, arrayTo, at, length);
    }


}
