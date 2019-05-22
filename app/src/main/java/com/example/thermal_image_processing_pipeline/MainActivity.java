package com.example.thermal_image_processing_pipeline;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.Network.thermal_image_processing_pipeline.TCPClient;
import com.google.android.material.textfield.TextInputEditText;
import com.log.log;
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionHOG;
import com.pipeline.thermal_image_processing_pipeline.MotionDetectionS;
import com.pipeline.thermal_image_processing_pipeline.Pipeline;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // General variables for streaming
    private PGMImage imageTemp;

    private Pipeline pipeline = null;

    private TCPClient tcpClient;

    public static ArrayList<byte[]> stream = new ArrayList<>();
    private ArrayList<PGMImage> imageStream = new ArrayList<>();
    private ArrayList<SubArray> subArrays = new ArrayList<>();

    private boolean run = true;

    private int[] dataAsInt;

    // Values for image manipulation.
    public static int brightness = 0, sharpening = 0;
    public static double contrast = 1.0;

    // Image pixels for width and height.
    public static int str_w = 0,  str_h = 0;

    // Data conversion threads' stuff.
    int AMOUNT_OF_THREADS_FOR_CONVERSION = 3;
    private boolean isAlive = true;

    // Motion sensor stuff.
    private TextView threshold, contourArea;
    private TextInputEditText thresholdInput, contourInput;
    private Switch sensorTypeSwitch;
    private final String THRESHOLD_TEXT = "Threshold value: ", CONTOURAREA_TEXT = "ContourArea value: ";
    public static int threshold_value = 25, contourArea_value = 500;
    public static boolean sensorType = false;


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
        pipeline = new Pipeline(MainActivity.this, 384, 288);


        // Setup seekBars
        {
            final SeekBar brightness = findViewById(R.id.seekBar1);
            final SeekBar contrast = findViewById(R.id.seekBar2);
            final SeekBar sharpening = findViewById(R.id.seekBar3);

            SeekBarListener seekBarListener = new SeekBarListener();
            brightness.setOnSeekBarChangeListener(seekBarListener);
            contrast.setOnSeekBarChangeListener(seekBarListener);
            sharpening.setOnSeekBarChangeListener(seekBarListener);
        }

        // Setup log
        {
            TextView txtView = findViewById(R.id.textView3);
            log.setTextView(txtView);
            log.setActivity(MainActivity.this);
        }

        // Setup motion sensor threshold and contour layout
        {
            threshold = findViewById(R.id.thresholdText);
            threshold.setText(THRESHOLD_TEXT + threshold_value);
            thresholdInput = findViewById(R.id.thresholdInput);
            Button clickButton = findViewById(R.id.buttonThreshold);
            clickButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateThreshold();
                }
            });

            contourArea = findViewById(R.id.contourAreaText);
            contourArea.setText(CONTOURAREA_TEXT + contourArea_value);
            contourInput = findViewById(R.id.contourInput);
            clickButton = findViewById(R.id.buttonContour);
            clickButton.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    updateContourArea();
                }
            });

            sensorTypeSwitch = findViewById(R.id.sensorType);
            sensorTypeSwitch.setOnClickListener( new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    changeSensor();
                }
            });
        }

        // Setup variables for the data conversion worker threads
        AMOUNT_OF_THREADS_FOR_CONVERSION = Runtime.getRuntime().availableProcessors();


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
                long timeStampStart, timeStampEnd;
                ImageView imgView = findViewById(R.id.imageView1);

                MotionDetectionS mdS = new MotionDetectionS();
                MotionDetectionHOG mdHOG = new MotionDetectionHOG();

                while(run){
                    if(stream != null && stream.size() > 0){
                        if(stream.size() > 0){
                            timeStampStart = System.currentTimeMillis();
                            imageTemp = new PGMImage(generateColorsFromImageBytes(stream.remove(0)));
                            pipeline.processImage(imageTemp);
                            imageStream.add(imageTemp);
                            timeStampEnd = System.currentTimeMillis();
                            log.setProcessImageDataTime(timeStampEnd-timeStampStart);

                            if (MainActivity.sensorType == false) {
                                mdS.detect(imageTemp);
                            } else {
                                mdHOG.detect(imageTemp);
                            }

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

    /**
     * Convert a frame's image data from raw data to an array of int.
     * @param imageData The frame's pixel data.
     * @return Processed data as an int array;
     */
    private int[] generateColorsFromImageBytes(final byte[] imageData){

        // Generate an amount of thread.
        if(imageData != null) {
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

            // Start the threads.
            for(SubArray sb : subArrays)
                sb.getT().start();

            // Set this thread to also do a part of the conversion.
            subArrays.add(new SubArray());
            generateColorsFromImageBytes(
                    imageData,
                    0,
                    0 + (str_h/AMOUNT_OF_THREADS_FOR_CONVERSION) * (AMOUNT_OF_THREADS_FOR_CONVERSION-1),
                    str_w,
                    str_h,
                    AMOUNT_OF_THREADS_FOR_CONVERSION-1
            );

            // Merge all the different sub arrays of int into the final int array. Also make sure the threads are dead.
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
            // Reset state.
            isAlive = true;
            subArrays.clear();

        }

        return dataAsInt;
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

    /**
     * Conver a section of the raw frame data into an array of int.
     * @param imageData The raw frame data.
     * @param wFrom Star width position.
     * @param hFrom Start height position.
     * @param wTo End width position.
     * @param hTo End height position.
     */
    private void generateColorsFromImageBytes(final byte[] imageData, int wFrom, int hFrom, int wTo, int hTo, final int pos){
        SubArray sbTemp = subArrays.get(pos);
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

        sbTemp.setAll(tempData,(hFrom*str_w), length);
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
     * Change the motion sensor's threshold value.
     */
    private void updateThreshold(){
        String s = "" + thresholdInput.getText();
        try {
            if(Integer.parseInt(s) != 0){
                threshold.setText(THRESHOLD_TEXT + s);
                threshold_value = Integer.parseInt(s);
            }
        }catch (NumberFormatException e){
            thresholdInput.setText("");
        }
    }

    /**
     * Change the motion sensor's contour area value.
     */
    private void updateContourArea(){
        String s = "" + contourInput.getText();
        try {
            if(Integer.parseInt(s) != 0){
                contourArea.setText(CONTOURAREA_TEXT + s);
                contourArea_value = Integer.parseInt(s);
            }
        }catch (NumberFormatException e){
            contourInput.setText("");
        }
    }

    /**
     * Switch from motion sensor to human detection sensor (or vice versa).
     */
    private void changeSensor(){
        sensorType = !sensorType;
    }

    /**
     * Send a task for the UI thread.
     * @param imgView The imageView to be updated.
     */
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
