package com.example.thermal_image_processing_pipeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.Network.thermal_image_processing_pipeline.TCPClient;
import com.log.log;
import com.pipeline.thermal_image_processing_pipeline.MeasureCenter;
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionBFS;
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionHOG;
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionMNET;
import com.pipeline.thermal_image_processing_pipeline.Pipeline;
import com.pipeline.thermal_image_processing_pipeline.RawImageData;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    //GUI
    private GUI gui;

    // General variables for streaming
    private PGMImage imageTemp;

    public static Pipeline pipeline = null;
    public static boolean getShutter = false;
    public static Activity activity;

    private TCPClient tcpClient;

    public static ArrayList<RawImageData> stream = new ArrayList<>();
    public static ArrayList<PGMImage> imageStreamOffline = new ArrayList<>();
    public static ArrayList<PGMImage> imageStream = new ArrayList<>();
    private ArrayList<SubArray> subArrays = new ArrayList<>();

    private boolean run = true;

    private int[] dataAsInt, dataRawAsInt;

    // Values for image manipulation.
    public static int brightness = 0, sharpening = 0;
    public static double contrast = 1.0;

    // Image pixels for width and height.
    public static int str_w = 384,  str_h = 288;

    // Data conversion threads' stuff.
    public static int MAX_THREADS = 3;
    private boolean isAlive = true;

    // Motion sensor stuff.
    public static int threshold_value = 25, contourArea_value = 500;
    public static int detectionType = 0;
    public static boolean detectionTypeChange = false;

    // Denoise
    public static boolean denoising = true;
    // Shutter and gain
    public static boolean shutterGain = true;
    // CLAHE
    public static boolean CLAHE = true;

    // Display False Color toggle.
    public static boolean FalseColor = false;

    //14-bit conversion
    public static double offset14bit = 0.;
    public static double gain14bit = 0.;

    //Shutter image settings
    public static long shutterTimeStamp = 0;
    public static long shutterTimeInterval = 12*60*1000;
    public static int NBR_SHUTTER_IMAGES = 8;

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

    /**
     * Setup device for use.
     */
    private void init(){

        activity = MainActivity.this;

        //Generate and setup GUI
        gui = new GUI(MainActivity.this );

        // Setup variables for the data conversion worker threads
        MAX_THREADS = Runtime.getRuntime().availableProcessors();

        pipeline = new Pipeline(MainActivity.this, 384, 288);


        StreamPlayer sp = new StreamPlayer(MainActivity.this, "test");
        //sp.play();
        //pipeline.getGain(MainActivity.this, str_w, str_h);
        //pipeline.setupShutterValueFromStorage(MainActivity.this);
    }


    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 0;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    /**
     * Startup of thread used for converting raw data to images.
     */
    private void generateBitmaps(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long timeStampStart, timeStampStart2, timeStampEnd;
                ImageView imgView = findViewById(R.id.imageView1);

                MotionDetectionBFS mdS = new MotionDetectionBFS();
                MotionDetectionHOG mdHOG = new MotionDetectionHOG();
                MotionDetectionMNET mdMNET = new MotionDetectionMNET(getAssets(), getFilesDir());
                MeasureCenter sMC = new MeasureCenter();

                int pos = 0;

                while(run){
                    if(stream != null && stream.size() > 0){
                        if(stream.size() > 0){

                            timeStampStart = System.currentTimeMillis();
                            imageTemp = generateColorsFromImageBytes(stream.remove(0));
                            //int i = imageTemp.getMaxValue();
                            timeStampEnd = System.currentTimeMillis();
                            log.shutterAndGainTime += timeStampEnd - timeStampStart;

                            pipeline.processImage(imageTemp);
                            imageStream.add(imageTemp);

                            log.imageCount++;
                            log.totalImageAmount++;
                            timeStampEnd = System.currentTimeMillis();
                            log.setProcessImageDataTime(timeStampEnd-timeStampStart);

                            if (MainActivity.detectionType == 1) {
                                sMC.detect(imageTemp);
                            } else if(MainActivity.detectionType == 2){
                                mdS.detect(imageTemp);
                            } else if(MainActivity.detectionType == 3){
                                mdHOG.detect(imageTemp);
                            } else if(MainActivity.detectionType == 4){
                                mdMNET.detect(imageTemp);
                            }

                            if (FalseColor == true) {
                                com.pipeline.thermal_image_processing_pipeline.FalseColor.color(imageTemp);
                            }


                        }
                    }else if(imageStreamOffline.size() > 0){
                        timeStampStart = System.currentTimeMillis();
                        // imageTemp = generateColorsFromImageBytes(stream.remove(0));
                        //Bitmap bShutter = DisplayHandler.generateBitmapFromArray(pipeline.getShutter());
                        imageTemp = new PGMImage(imageStreamOffline.get(pos).getDataList());
                        //Bitmap b = DisplayHandler.generateBitmapFromArray(imageTemp.getDataList());
                        pipeline.applyShutterAndGainToImage(imageTemp);
                        //Bitmap b2 = DisplayHandler.generateBitmapFromPGM(imageTemp);
                        pipeline.processImage(imageTemp);
                        //Bitmap b3 = DisplayHandler.generateBitmapFromPGM(imageTemp);
                        imageStream.add(imageTemp);
                        timeStampEnd = System.currentTimeMillis();
                        log.setProcessImageDataTime(timeStampEnd-timeStampStart);

                        if (MainActivity.detectionType == 1) {
                            sMC.detect(imageTemp);
                        } else if(MainActivity.detectionType == 2){
                            mdS.detect(imageTemp);
                        } else if(MainActivity.detectionType == 3){
                            mdHOG.detect(imageTemp);
                        } else if(MainActivity.detectionType == 4){
                            mdMNET.detect(imageTemp);
                        }

                        if (FalseColor == true) {
                            com.pipeline.thermal_image_processing_pipeline.FalseColor.color(imageTemp);
                        }

                        pos++;
                        if(pos >= imageStreamOffline.size()) pos=0;
                    }

                    if(imageStream.size() > 0)
                        gui.updateView(MainActivity.this, imgView);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("PIPELINE_THREAD");
        thread.start();
    }

    /**
     * Convert a frame's image data from raw data to an array of int.
     * @param imageData The frame's pixel data.
     * @return Processed data as an int array;
     */
    private PGMImage generateColorsFromImageBytes(final RawImageData imageData){

        // Generate an amount of thread.
        if(imageData != null) {
            dataAsInt = new int[str_h*str_w];
            dataRawAsInt = new int[str_h*str_w];
            for(int i = 0; i < MAX_THREADS -1; i++){
                subArrays.add(new SubArray(generateColorsFromImagesBytesWithinRange(
                        imageData,
                        0,
                        0 + (str_h/ MAX_THREADS) * i,
                        str_w,
                        (str_h/ MAX_THREADS) * (i + 1),
                        i)
                ));
            }

            // Start the threads.
            for(SubArray sb : subArrays)
                sb.getT().start();

            // Set this thread to also do a part of the conversion.
            subArrays.add(new SubArray());
            generateColorsFromImageBytes(
                    imageData,
                    0,
                    0 + (str_h/ MAX_THREADS) * (MAX_THREADS -1),
                    str_w,
                    str_h,
                    MAX_THREADS -1
            );

            // Merge all the different sub arrays of int into the final int array. Also make sure the threads are dead.
            while(isAlive){
                isAlive = false;
                for(SubArray sb : subArrays){
                    if(sb.getT() != null){
                        if(!sb.getT().isAlive()){
                            addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength());
                            addDataFromArray(dataRawAsInt, sb.getDataRaw(), sb.getStart(), sb.getLength());
                        }
                        else
                            isAlive = true;
                    }else{
                        addDataFromArray(dataAsInt, sb.getData(), sb.getStart(), sb.getLength());
                        addDataFromArray(dataRawAsInt, sb.getDataRaw(), sb.getStart(), sb.getLength());
                    }
                }
            }
            // Reset state.
            isAlive = true;
            subArrays.clear();

        }

        return new PGMImage(dataAsInt, dataRawAsInt);
    }

    /**
     * Generate a thread with which to convert a section of the raw image data to an int array.
     * @param imageData The raw frame data.
     * @param wFrom Star width position.
     * @param hFrom Start height position.
     * @param wTo End width position.
     * @param hTo End height position.
     * @param threadName Name of the thread.
     * @return The generated thread.
     */
    private Thread generateColorsFromImagesBytesWithinRange(final RawImageData imageData, final int wFrom, final int hFrom, final int wTo, final int hTo, final int threadName){
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

    /**
     * Conver a section of the raw frame data into an array of int.
     * @param RawImage The container class for the frame data.
     * @param wFrom Star width position.
     * @param hFrom Start height position.
     * @param wTo End width position.
     * @param hTo End height position.
     */
    private void generateColorsFromImageBytes(final RawImageData RawImage, int wFrom, int hFrom, int wTo, int hTo, final int pos){
        SubArray sbTemp = subArrays.get(pos);
        int length = (wTo-wFrom)*(hTo-hFrom);
        byte[] imageData = RawImage.getData();
        int[] tempData = new int[length];
        int[] tempDataRaw = new int[length];
        final int[] shutterValues = pipeline.getShutter();
        final float[] gain = pipeline.getGain();
        final int mean = pipeline.getMean();
        int temp, b1, b2 = 0, b3 = 0, colorValue;
        int dataIndex = (int)(hFrom*str_w*1.5);
        double gain14bit = RawImage.getGain();


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
                //temp = (int)((temp + offset14bit)/gain14bit);
                tempDataRaw[((h-hFrom)*str_w) + (w-wFrom)] = temp;
                if(shutterGain) temp = (int)( (float)( (temp - (shutterValues[(h*str_w) + w] * gain14bit))) * gain[(h*str_w) + w] + (mean * gain14bit));
                colorValue = (int)(((double)temp / 4096.0) * 255);
                // tempDataRaw[((h-hFrom)*str_w) + (w-wFrom)] = temp;
                tempData[((h-hFrom)*str_w) + (w-wFrom)] = 0xff000000 | (colorValue << 16) | (colorValue << 8) | colorValue;
            }

        sbTemp.setAll(tempData, tempDataRaw, (hFrom*str_w), length);
    }

    /**
     * Copy an array into another.
     * @param arrayTo Destination array.
     * @param arrayFrom Source array.
     * @param at Start at position.
     * @param length Length of the data to copy.
     */
    private void addDataFromArray(int[] arrayTo, int[] arrayFrom, int at, int length) {
        System.arraycopy(arrayFrom, 0, arrayTo, at, length);
    }

    /**
     * Asynchronous task for TCP connection.
     */
    private class ConnectTask extends AsyncTask<String, String, TCPClient> {

        @Override
        protected TCPClient doInBackground(String... message) {

            //we create a TCPClient object
            tcpClient = new TCPClient();
            tcpClient.StartReadingRawStream();

            Log.d("TCP Client", "Session ended.");
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
