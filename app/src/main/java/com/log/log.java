package com.log;

import android.app.Activity;
import android.os.Environment;
import android.widget.TextView;


import com.example.thermal_image_processing_pipeline.MainActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Custom class for displaying something onto a textView.
 */
public class log {
    private static TextView output;
    private static Activity a;
    private static ArrayList<String> inputs = new ArrayList<>();
    public static long imageDataTime, processImageTime, getImageDataTime, processImageDataTime, FPSTimeStamp = 0, shutterAndGainTime = 0, CLAHETime = 0, denoiseTime = 0, filterTime = 0;
    public static int amountInStream, totalImageAmount, imageCount, FPS;
    public static long startTime, stopTime;

    public static void setTextView(final TextView tv){
        output = tv;
    }

    public static void setActivity(final Activity activity){
        a = activity;
    }

    public static void setGetImageDataTime(final long input){
        getImageDataTime = input;
        imageDataTime += input;
    }

    public static void setProcessImageDataTime(final long input){
        processImageDataTime = input;
        processImageTime += input;
    }

    public static void setAmountInStream(final int input){
        amountInStream = input;
    }

    public static void writeToOutputs(){
        a.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                output.setText("Time to get image data: " +getImageDataTime + "\nTime to process image data: " + processImageDataTime +  "\nAmount in stream: " + amountInStream +
                        "\nFPS: " + FPS +
                        "\nAverage time for retrieving data: " + (imageDataTime / totalImageAmount) + " ms." +
                        "\nAverage time for processing data: " + (processImageTime / totalImageAmount) + " ms." +
                        "\nAverage time for shutter and gain: " + (shutterAndGainTime/totalImageAmount) + " ms." +
                        "\nAverage time for CLAHE: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\nAverage time to denoise: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\nAverage time to apply filter: " + (filterTime/totalImageAmount) + " ms." +
                        "\nTotal images processed: " + totalImageAmount +
                        "\n\n");

                //logToFile();

            }
        });
    }

    public static void checkFPS(){
        if(FPSTimeStamp == 0) FPSTimeStamp = System.currentTimeMillis();

        if(System.currentTimeMillis() - FPSTimeStamp >= 1000){
            FPS = imageCount;
            imageCount = 0;
            FPSTimeStamp = System.currentTimeMillis();
        }
    }

    private static void logToFile() {

        if (startTime == 0 && totalImageAmount != 0) {
            startTime = System.currentTimeMillis();
        }

        if (totalImageAmount == 1000) {

            stopTime = System.currentTimeMillis();

            long runTime = (stopTime - startTime);
            long seconds = (runTime / 1000) % 60;
            long minutes = ((runTime / (1000*60)) % 60);
            long hours = ((runTime / (1000*60*60)) % 24);

            double runTimeInSecs = runTime / 1000.0;
            double averageFPS = totalImageAmount / runTimeInSecs;

            String filename = "Basic settings only.txt";

            File sdcard = Environment.getExternalStorageDirectory();
            File file = new File(sdcard, "/Download/" + filename);

            try {
                BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(file, true));
                outFile.write(("Time to get image data: " +getImageDataTime +
                        "\r\nTime to process image data: " + processImageDataTime +
                        "\r\nAmount in stream: " + amountInStream +
                        "\r\nFPS: " + FPS +
                        "\r\nAverage time for retrieving data: " + (imageDataTime / totalImageAmount) + " ms." +
                        "\r\nAverage time for processing data: " + (processImageTime / totalImageAmount) + " ms." +
                        "\r\nAverage time for shutter and gain: " + (shutterAndGainTime/totalImageAmount) + " ms." +
                        "\r\nAverage time for CLAHE: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\r\nAverage time to denoise: " + (CLAHETime/totalImageAmount) + " ms." +
                        "\r\nAverage time to apply filter: " + (filterTime/totalImageAmount) + " ms." +
                        "\r\nTotal images processed: " + totalImageAmount +
                        "\r\nTotal run time: " + runTime + " ms (" + hours + " h " + minutes + " min " + seconds + " sec) " +
                        "\r\nAverage FPS: " + averageFPS).getBytes());
                outFile.close();
            } catch (IOException e) {
                // Do nothing.
            }
        }

    }

}
